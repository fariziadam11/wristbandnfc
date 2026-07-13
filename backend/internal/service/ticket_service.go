package service

import (
	"errors"
	"time"

	"github.com/google/uuid"
	"github.com/gbs/nfc-wristband-backend/internal/dto"
	"github.com/gbs/nfc-wristband-backend/internal/model"
	"github.com/gbs/nfc-wristband-backend/internal/repository"
	"gorm.io/gorm"
)

var (
	ErrTicketNotFound    = errors.New("ticket not found")
	ErrNoValidTickets    = errors.New("no valid tickets found")
	ErrTicketAlreadyUsed = errors.New("ticket already used")
	ErrTicketExpired     = errors.New("ticket expired")
)

type TicketService struct {
	db            *gorm.DB
	wristbandRepo *repository.WristbandRepository
	ticketRepo    *repository.TicketRepository
	auditRepo     *repository.AuditLogRepository
}

func NewTicketService(
	db *gorm.DB,
	wristbandRepo *repository.WristbandRepository,
	ticketRepo *repository.TicketRepository,
	auditRepo *repository.AuditLogRepository,
) *TicketService {
	return &TicketService{
		db:            db,
		wristbandRepo: wristbandRepo,
		ticketRepo:    ticketRepo,
		auditRepo:     auditRepo,
	}
}

func (s *TicketService) Validate(req dto.ValidateTicketRequest, petugasID uuid.UUID) (*dto.ValidateTicketResponse, error) {
	// Find wristband by UID and token
	wristband, err := s.wristbandRepo.FindByUIDAndToken(req.UID, req.Token)
	if err != nil {
		return &dto.ValidateTicketResponse{
			Result:  "not_found",
			Message: "Wristband not registered",
		}, ErrTicketNotFound
	}

	var response *dto.ValidateTicketResponse

	err = s.db.Transaction(func(tx *gorm.DB) error {
		// Find valid tickets
		tickets, err := s.ticketRepo.WithTx(tx).FindActiveByWristbandID(wristband.ID)
		if err != nil {
			return err
		}

		if len(tickets) == 0 {
			return ErrNoValidTickets
		}

		// Get the first valid ticket
		ticket := tickets[0]
		now := time.Now()

		// Check if ticket has expired (even if status is valid)
		if ticket.ValidUntil != nil && ticket.ValidUntil.Before(now) {
			// Mark as expired
			tx.Model(&model.Ticket{}).Where("id = ?", ticket.ID).Update("status", model.TicketExpired)
			response = &dto.ValidateTicketResponse{
				Result:     "expired",
				TicketType: ticket.TicketType,
				ValidUntil: ticket.ValidUntil,
				Message:    "Ticket has expired",
			}
			return ErrTicketExpired
		}

		// Mark ticket as used
		if err := s.ticketRepo.WithTx(tx).MarkAsUsed(tx, ticket.ID, petugasID); err != nil {
			return err
		}

		usedAt := time.Now()
		response = &dto.ValidateTicketResponse{
			Result:     "valid",
			TicketType: ticket.TicketType,
			ValidUntil: ticket.ValidUntil,
			UsedAt:     &usedAt,
			Message:    "Ticket validated successfully",
		}

		return nil
	})

	if err != nil {
		if errors.Is(err, ErrTicketAlreadyUsed) {
			return &dto.ValidateTicketResponse{
				Result:  "already_used",
				Message: "Ticket has already been used",
			}, nil
		}
		if errors.Is(err, ErrTicketExpired) {
			return response, nil
		}
		if errors.Is(err, ErrNoValidTickets) {
			return &dto.ValidateTicketResponse{
				Result:  "no_valid_ticket",
				Message: "No valid ticket found for this wristband",
			}, nil
		}
		return nil, err
	}

	// Audit log
	s.auditRepo.Create(&model.AuditLog{
		ActorUserID: petugasID,
		Action:     "validate_ticket",
		Entity:     "ticket",
		EntityID:   wristband.ID.String(),
		Metadata: map[string]interface{}{
			"result":      response.Result,
			"ticket_type": response.TicketType,
		},
	})

	return response, nil
}

func (s *TicketService) GetTickets(wristbandID uuid.UUID) ([]dto.TicketResponse, error) {
	tickets, err := s.ticketRepo.FindByWristbandID(wristbandID)
	if err != nil {
		return nil, err
	}

	responses := make([]dto.TicketResponse, len(tickets))
	for i, t := range tickets {
		responses[i] = dto.TicketResponse{
			ID:          t.ID,
			TicketType:  t.TicketType,
			Description: t.Description,
			Price:       t.Price,
			Status:      string(t.Status),
			ValidFrom:   t.ValidFrom,
			ValidUntil:  t.ValidUntil,
			UsedAt:      t.UsedAt,
		}
	}

	return responses, nil
}

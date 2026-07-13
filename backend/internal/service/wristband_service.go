package service

import (
	"errors"
	"time"

	"github.com/google/uuid"
	"github.com/gbs/nfc-wristband-backend/internal/dto"
	"github.com/gbs/nfc-wristband-backend/internal/model"
	"github.com/gbs/nfc-wristband-backend/internal/repository"
	"github.com/gbs/nfc-wristband-backend/pkg/utils"
)

var (
	ErrWristbandNotFound       = errors.New("wristband not found")
	ErrWristbandAlreadyExists  = errors.New("wristband with this UID already exists")
	ErrInvalidToken           = errors.New("invalid token")
	ErrUIDMismatch             = errors.New("UID mismatch - possible tag clone")
)

type WristbandService struct {
	wristbandRepo *repository.WristbandRepository
	userRepo     *repository.UserRepository
	walletRepo   *repository.WalletRepository
	ticketRepo   *repository.TicketRepository
	auditRepo    *repository.AuditLogRepository
}

func NewWristbandService(
	wristbandRepo *repository.WristbandRepository,
	userRepo *repository.UserRepository,
	walletRepo *repository.WalletRepository,
	ticketRepo *repository.TicketRepository,
	auditRepo *repository.AuditLogRepository,
) *WristbandService {
	return &WristbandService{
		wristbandRepo: wristbandRepo,
		userRepo:      userRepo,
		walletRepo:    walletRepo,
		ticketRepo:    ticketRepo,
		auditRepo:     auditRepo,
	}
}

func (s *WristbandService) Register(req dto.RegisterWristbandRequest, registeredBy uuid.UUID) (*dto.RegisterWristbandResponse, error) {
	// Generate unique UID (in real app, this comes from NFC scan)
	uid, err := utils.GenerateToken(4)
	if err != nil {
		return nil, err
	}

	// Check if wristband with this UID already exists
	_, err = s.wristbandRepo.FindByUID(uid)
	if err == nil {
		return nil, ErrWristbandAlreadyExists
	}

	// Generate token for NDEF writing
	token, err := utils.GenerateShortToken()
	if err != nil {
		return nil, err
	}

	// Create customer user
	customer := &model.User{
		Name:  req.CustomerName,
		Email: req.CustomerEmail,
		Role:  model.RoleCustomer,
	}
	if customer.Email == "" {
		customer.Email = "customer-" + uuid.New().String() + "@nfc.local"
	}

	if err := s.userRepo.Create(customer); err != nil {
		return nil, err
	}

	// Create wristband
	wristband := &model.Wristband{
		UID:          uid,
		Token:        token,
		CustomerID:   customer.ID,
		RegisteredBy: registeredBy,
		Status:       model.StatusActive,
		IsWritten:    false,
	}

	if err := s.wristbandRepo.Create(wristband); err != nil {
		return nil, err
	}

	// Create wallet automatically
	wallet := &model.Wallet{
		WristbandID: wristband.ID,
		Balance:     0,
		Currency:    "IDR",
	}

	if err := s.walletRepo.Create(wallet); err != nil {
		return nil, err
	}

	// Create a default ticket for demo
	validUntil := time.Now().Add(24 * time.Hour)
	ticket := &model.Ticket{
		WristbandID: wristband.ID,
		TicketType:  "1-day-pass",
		Description: "Demo ticket - valid for 1 day",
		Price:       50000,
		Status:      model.TicketValid,
		ValidFrom:   time.Now(),
		ValidUntil:  &validUntil,
	}

	if err := s.ticketRepo.Create(ticket); err != nil {
		return nil, err
	}

	// Audit log
	s.auditRepo.Create(&model.AuditLog{
		ActorUserID: registeredBy,
		Action:     "register_wristband",
		Entity:     "wristband",
		EntityID:   wristband.ID.String(),
		Metadata: map[string]interface{}{
			"customer_name": req.CustomerName,
			"uid":          uid,
			"token":        token,
		},
	})

	return &dto.RegisterWristbandResponse{
		ID:           wristband.ID,
		UID:          uid,
		Token:        token,
		CustomerID:   customer.ID,
		CustomerName: req.CustomerName,
		RegisteredBy: registeredBy,
	}, nil
}

func (s *WristbandService) WriteNFC(uid string) (*dto.WriteWristbandResponse, error) {
	// Find wristband by UID
	wristband, err := s.wristbandRepo.FindByUID(uid)
	if err != nil {
		return nil, ErrWristbandNotFound
	}

	// Generate new token for writing
	token, err := utils.GenerateShortToken()
	if err != nil {
		return nil, err
	}

	// Update wristband with new token
	wristband.Token = token
	wristband.IsWritten = true

	if err := s.wristbandRepo.Update(wristband); err != nil {
		return nil, err
	}

	return &dto.WriteWristbandResponse{
		UID:     uid,
		Token:   token,
		Message: "Token written to NFC tag successfully",
	}, nil
}

func (s *WristbandService) Scan(uid, token string) (*dto.ScanWristbandResponse, error) {
	// Validate UID AND token
	wristband, err := s.wristbandRepo.FindByUIDAndToken(uid, token)
	if err != nil {
		return nil, ErrWristbandNotFound
	}

	if wristband.Status != model.StatusActive {
		return nil, errors.New("wristband is " + string(wristband.Status))
	}

	// Get active tickets
	activeTickets, _ := s.ticketRepo.FindActiveByWristbandID(wristband.ID)

	ticketResponses := make([]dto.TicketResponse, len(activeTickets))
	for i, t := range activeTickets {
		ticketResponses[i] = dto.TicketResponse{
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

	return &dto.ScanWristbandResponse{
		WristbandID: wristband.ID,
		UID:         wristband.UID,
		Token:       wristband.Token,
		Status:      string(wristband.Status),
		Customer: dto.CustomerResponse{
			ID:   wristband.Customer.ID,
			Name: wristband.Customer.Name,
		},
		Wallet: dto.WalletResponse{
			ID:       wristband.Wallet.ID,
			Balance:  wristband.Wallet.Balance,
			Currency: wristband.Wallet.Currency,
		},
		ActiveTickets: ticketResponses,
	}, nil
}

func (s *WristbandService) GetByWristbandID(wristbandID uuid.UUID) (*model.Wristband, error) {
	return s.wristbandRepo.FindByID(wristbandID)
}

func (s *WristbandService) GetWallet(wristbandID uuid.UUID) (*dto.GetWalletResponse, error) {
	wristband, err := s.wristbandRepo.FindByID(wristbandID)
	if err != nil {
		return nil, ErrWristbandNotFound
	}

	return &dto.GetWalletResponse{
		ID:       wristband.Wallet.ID,
		Balance:  wristband.Wallet.Balance,
		Currency: wristband.Wallet.Currency,
		UID:      wristband.UID,
	}, nil
}

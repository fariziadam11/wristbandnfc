package repository

import (
	"time"

	"github.com/google/uuid"
	"github.com/gbs/nfc-wristband-backend/internal/model"
	"gorm.io/gorm"
)

type TicketRepository struct {
	db *gorm.DB
}

func NewTicketRepository(db *gorm.DB) *TicketRepository {
	return &TicketRepository{db: db}
}

func (r *TicketRepository) Create(ticket *model.Ticket) error {
	return r.db.Create(ticket).Error
}

func (r *TicketRepository) FindByID(id uuid.UUID) (*model.Ticket, error) {
	var ticket model.Ticket
	err := r.db.First(&ticket, "id = ?", id).Error
	if err != nil {
		return nil, err
	}
	return &ticket, nil
}

func (r *TicketRepository) FindActiveByWristbandID(wristbandID uuid.UUID) ([]model.Ticket, error) {
	var tickets []model.Ticket
	now := time.Now()
	err := r.db.Where("wristband_id = ? AND status = ? AND (valid_until IS NULL OR valid_until > ?)",
		wristbandID, model.TicketValid, now).
		Find(&tickets).Error
	return tickets, err
}

func (r *TicketRepository) FindByWristbandID(wristbandID uuid.UUID) ([]model.Ticket, error) {
	var tickets []model.Ticket
	err := r.db.Where("wristband_id = ?", wristbandID).Order("created_at DESC").Find(&tickets).Error
	return tickets, err
}

func (r *TicketRepository) MarkAsUsed(tx *gorm.DB, id uuid.UUID, usedBy uuid.UUID) error {
	now := time.Now()
	return tx.Model(&model.Ticket{}).Where("id = ?", id).Updates(map[string]interface{}{
		"status":  model.TicketUsed,
		"used_at": now,
		"used_by": usedBy,
	}).Error
}

func (r *TicketRepository) Update(ticket *model.Ticket) error {
	return r.db.Save(ticket).Error
}

func (r *TicketRepository) WithTx(tx *gorm.DB) *TicketRepository {
	return &TicketRepository{db: tx}
}

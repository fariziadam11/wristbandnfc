package repository

import (
	"github.com/google/uuid"
	"github.com/gbs/nfc-wristband-backend/internal/model"
	"gorm.io/gorm"
)

type AuditLogRepository struct {
	db *gorm.DB
}

func NewAuditLogRepository(db *gorm.DB) *AuditLogRepository {
	return &AuditLogRepository{db: db}
}

func (r *AuditLogRepository) Create(log *model.AuditLog) error {
	return r.db.Create(log).Error
}

func (r *AuditLogRepository) CreateWithTx(tx *gorm.DB, log *model.AuditLog) error {
	return tx.Create(log).Error
}

func (r *AuditLogRepository) FindByActorUserID(userID uuid.UUID, limit int) ([]model.AuditLog, error) {
	var logs []model.AuditLog
	query := r.db.Where("actor_user_id = ?", userID).Order("created_at DESC")
	if limit > 0 {
		query = query.Limit(limit)
	}
	err := query.Find(&logs).Error
	return logs, err
}

func (r *AuditLogRepository) FindAll(limit int) ([]model.AuditLog, error) {
	var logs []model.AuditLog
	query := r.db.Order("created_at DESC")
	if limit > 0 {
		query = query.Limit(limit)
	}
	err := query.Find(&logs).Error
	return logs, err
}

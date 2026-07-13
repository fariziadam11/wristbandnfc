package repository

import (
	"github.com/google/uuid"
	"github.com/gbs/nfc-wristband-backend/internal/model"
	"gorm.io/gorm"
)

type TransactionRepository struct {
	db *gorm.DB
}

func NewTransactionRepository(db *gorm.DB) *TransactionRepository {
	return &TransactionRepository{db: db}
}

func (r *TransactionRepository) Create(tx *gorm.DB, transaction *model.Transaction) error {
	return tx.Create(transaction).Error
}

func (r *TransactionRepository) FindByID(id uuid.UUID) (*model.Transaction, error) {
	var transaction model.Transaction
	err := r.db.First(&transaction, "id = ?", id).Error
	if err != nil {
		return nil, err
	}
	return &transaction, nil
}

func (r *TransactionRepository) FindByWalletID(walletID uuid.UUID, limit int) ([]model.Transaction, error) {
	var transactions []model.Transaction
	query := r.db.Where("wallet_id = ?", walletID).Order("created_at DESC")
	if limit > 0 {
		query = query.Limit(limit)
	}
	err := query.Find(&transactions).Error
	return transactions, err
}

func (r *TransactionRepository) FindByWristbandID(wristbandID uuid.UUID, limit int) ([]model.Transaction, error) {
	var transactions []model.Transaction
	query := r.db.Joins("JOIN wallets ON wallets.id = transactions.wallet_id").
		Where("wallets.wristband_id = ?", wristbandID).
		Order("transactions.created_at DESC")
	if limit > 0 {
		query = query.Limit(limit)
	}
	err := query.Find(&transactions).Error
	return transactions, err
}

func (r *TransactionRepository) CountByWalletID(walletID uuid.UUID) (int64, error) {
	var count int64
	err := r.db.Model(&model.Transaction{}).Where("wallet_id = ?", walletID).Count(&count).Error
	return count, err
}

package repository

import (
	"github.com/google/uuid"
	"github.com/gbs/nfc-wristband-backend/internal/model"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

type WalletRepository struct {
	db *gorm.DB
}

func NewWalletRepository(db *gorm.DB) *WalletRepository {
	return &WalletRepository{db: db}
}

func (r *WalletRepository) Create(wallet *model.Wallet) error {
	return r.db.Create(wallet).Error
}

func (r *WalletRepository) FindByID(id uuid.UUID) (*model.Wallet, error) {
	var wallet model.Wallet
	err := r.db.First(&wallet, "id = ?", id).Error
	if err != nil {
		return nil, err
	}
	return &wallet, nil
}

func (r *WalletRepository) FindByWristbandID(wristbandID uuid.UUID) (*model.Wallet, error) {
	var wallet model.Wallet
	err := r.db.First(&wallet, "wristband_id = ?", wristbandID).Error
	if err != nil {
		return nil, err
	}
	return &wallet, nil
}

// FindByWristbandIDForUpdate finds wallet with row lock for update
func (r *WalletRepository) FindByWristbandIDForUpdate(tx *gorm.DB, wristbandID uuid.UUID) (*model.Wallet, error) {
	var wallet model.Wallet
	err := tx.Clauses(clause.Locking{Strength: "UPDATE"}).First(&wallet, "wristband_id = ?", wristbandID).Error
	if err != nil {
		return nil, err
	}
	return &wallet, nil
}

func (r *WalletRepository) Update(wallet *model.Wallet) error {
	return r.db.Save(wallet).Error
}

func (r *WalletRepository) UpdateBalance(tx *gorm.DB, id uuid.UUID, newBalance int64) error {
	return tx.Model(&model.Wallet{}).Where("id = ?", id).Update("balance", newBalance).Error
}

func (r *WalletRepository) WithTx(tx *gorm.DB) *WalletRepository {
	return &WalletRepository{db: tx}
}

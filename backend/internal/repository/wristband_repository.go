package repository

import (
	"github.com/google/uuid"
	"github.com/gbs/nfc-wristband-backend/internal/model"
	"gorm.io/gorm"
)

type WristbandRepository struct {
	db *gorm.DB
}

func NewWristbandRepository(db *gorm.DB) *WristbandRepository {
	return &WristbandRepository{db: db}
}

func (r *WristbandRepository) Create(wristband *model.Wristband) error {
	return r.db.Create(wristband).Error
}

func (r *WristbandRepository) FindByID(id uuid.UUID) (*model.Wristband, error) {
	var wristband model.Wristband
	err := r.db.Preload("Customer").Preload("Wallet").First(&wristband, "id = ?", id).Error
	if err != nil {
		return nil, err
	}
	return &wristband, nil
}

func (r *WristbandRepository) FindByUID(uid string) (*model.Wristband, error) {
	var wristband model.Wristband
	err := r.db.Preload("Customer").Preload("Wallet").First(&wristband, "uid = ?", uid).Error
	if err != nil {
		return nil, err
	}
	return &wristband, nil
}

func (r *WristbandRepository) FindByToken(token string) (*model.Wristband, error) {
	var wristband model.Wristband
	err := r.db.Preload("Customer").Preload("Wallet").First(&wristband, "token = ?", token).Error
	if err != nil {
		return nil, err
	}
	return &wristband, nil
}

func (r *WristbandRepository) FindByUIDAndToken(uid, token string) (*model.Wristband, error) {
	var wristband model.Wristband
	err := r.db.Preload("Customer").Preload("Wallet").Preload("Tickets").
		First(&wristband, "uid = ? AND token = ?", uid, token).Error
	if err != nil {
		return nil, err
	}
	return &wristband, nil
}

func (r *WristbandRepository) Update(wristband *model.Wristband) error {
	return r.db.Save(wristband).Error
}

func (r *WristbandRepository) UpdateStatus(id uuid.UUID, status model.WristbandStatus) error {
	return r.db.Model(&model.Wristband{}).Where("id = ?", id).Update("status", status).Error
}

func (r *WristbandRepository) MarkAsWritten(uid string) error {
	return r.db.Model(&model.Wristband{}).Where("uid = ?", uid).Update("is_written", true).Error
}

func (r *WristbandRepository) FindByCustomerID(customerID uuid.UUID) ([]model.Wristband, error) {
	var wristbands []model.Wristband
	err := r.db.Where("customer_id = ?", customerID).Find(&wristbands).Error
	return wristbands, err
}

func (r *WristbandRepository) WithTx(tx *gorm.DB) *WristbandRepository {
	return &WristbandRepository{db: tx}
}

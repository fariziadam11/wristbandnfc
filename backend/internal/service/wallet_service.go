package service

import (
	"errors"

	"github.com/google/uuid"
	"github.com/gbs/nfc-wristband-backend/internal/dto"
	"github.com/gbs/nfc-wristband-backend/internal/model"
	"github.com/gbs/nfc-wristband-backend/internal/repository"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

var (
	ErrInsufficientBalance = errors.New("insufficient balance")
	ErrWalletNotFound     = errors.New("wallet not found")
	ErrInvalidAmount       = errors.New("invalid amount")
)

type WalletService struct {
	db             *gorm.DB
	walletRepo     *repository.WalletRepository
	wristbandRepo  *repository.WristbandRepository
	transactionRepo *repository.TransactionRepository
	auditRepo      *repository.AuditLogRepository
}

func NewWalletService(
	db *gorm.DB,
	walletRepo *repository.WalletRepository,
	wristbandRepo *repository.WristbandRepository,
	transactionRepo *repository.TransactionRepository,
	auditRepo *repository.AuditLogRepository,
) *WalletService {
	return &WalletService{
		db:             db,
		walletRepo:     walletRepo,
		wristbandRepo:  wristbandRepo,
		transactionRepo: transactionRepo,
		auditRepo:      auditRepo,
	}
}

func (s *WalletService) Topup(req dto.TopupRequest, petugasID uuid.UUID) (*dto.TopupResponse, error) {
	if req.Amount <= 0 {
		return nil, ErrInvalidAmount
	}

	// Find wristband by UID and token
	wristband, err := s.wristbandRepo.FindByUIDAndToken(req.UID, req.Token)
	if err != nil {
		return nil, ErrWristbandNotFound
	}

	var response *dto.TopupResponse

	err = s.db.Transaction(func(tx *gorm.DB) error {
		// Get wallet with lock
		var wallet model.Wallet
		if err := tx.Clauses(clause.Locking{Strength: "UPDATE"}).First(&wallet, "wristband_id = ?", wristband.ID).Error; err != nil {
			return ErrWalletNotFound
		}

		previousBalance := wallet.Balance
		newBalance := previousBalance + req.Amount

		// Update wallet balance
		if err := tx.Model(&model.Wallet{}).Where("id = ?", wallet.ID).Update("balance", newBalance).Error; err != nil {
			return err
		}

		// Create transaction record
		transaction := &model.Transaction{
			WalletID:      wallet.ID,
			Type:          model.TransactionTopup,
			Amount:        req.Amount,
			BalanceBefore: previousBalance,
			BalanceAfter:  newBalance,
			PetugasID:     petugasID,
		}

		if err := tx.Create(transaction).Error; err != nil {
			return err
		}

		response = &dto.TopupResponse{
			TransactionID:   transaction.ID,
			WristbandID:     wristband.ID,
			PreviousBalance: previousBalance,
			Amount:          req.Amount,
			NewBalance:      newBalance,
		}

		return nil
	})

	if err != nil {
		return nil, err
	}

	// Audit log
	s.auditRepo.Create(&model.AuditLog{
		ActorUserID: petugasID,
		Action:     "topup",
		Entity:     "wallet",
		EntityID:   response.WristbandID.String(),
		Metadata: map[string]interface{}{
			"amount":           req.Amount,
			"new_balance":      response.NewBalance,
			"transaction_id":   response.TransactionID.String(),
		},
	})

	return response, nil
}

func (s *WalletService) Payment(req dto.PaymentRequest, petugasID uuid.UUID) (*dto.PaymentResponse, error) {
	if req.Amount <= 0 {
		return nil, ErrInvalidAmount
	}

	// Find wristband by UID and token
	wristband, err := s.wristbandRepo.FindByUIDAndToken(req.UID, req.Token)
	if err != nil {
		return nil, ErrWristbandNotFound
	}

	var response *dto.PaymentResponse

	err = s.db.Transaction(func(tx *gorm.DB) error {
		// Get wallet with lock
		var wallet model.Wallet
		if err := tx.Clauses(clause.Locking{Strength: "UPDATE"}).First(&wallet, "wristband_id = ?", wristband.ID).Error; err != nil {
			return ErrWalletNotFound
		}

		// Check balance
		if wallet.Balance < req.Amount {
			return ErrInsufficientBalance
		}

		previousBalance := wallet.Balance
		newBalance := previousBalance - req.Amount

		// Update wallet balance
		if err := tx.Model(&model.Wallet{}).Where("id = ?", wallet.ID).Update("balance", newBalance).Error; err != nil {
			return err
		}

		// Create transaction record
		transaction := &model.Transaction{
			WalletID:      wallet.ID,
			Type:          model.TransactionPayment,
			Amount:        req.Amount,
			BalanceBefore: previousBalance,
			BalanceAfter:  newBalance,
			ReferenceID:   req.ReferenceID,
			PetugasID:     petugasID,
		}

		if err := tx.Create(transaction).Error; err != nil {
			return err
		}

		response = &dto.PaymentResponse{
			TransactionID:   transaction.ID,
			Status:          string(model.TransactionSuccess),
			PreviousBalance: previousBalance,
			Amount:          req.Amount,
			NewBalance:      newBalance,
		}

		return nil
	})

	if err != nil {
		if errors.Is(err, ErrInsufficientBalance) {
			wallet, _ := s.walletRepo.FindByWristbandID(wristband.ID)
			return &dto.PaymentResponse{
				Status:         "failed",
				PreviousBalance: wallet.Balance,
			}, ErrInsufficientBalance
		}
		return nil, err
	}

	// Audit log
	s.auditRepo.Create(&model.AuditLog{
		ActorUserID: petugasID,
		Action:     "payment",
		Entity:     "wallet",
		EntityID:   wristband.ID.String(),
		Metadata: map[string]interface{}{
			"amount":          req.Amount,
			"previous_balance": response.PreviousBalance,
			"new_balance":     response.NewBalance,
			"reference_id":    req.ReferenceID,
			"transaction_id":  response.TransactionID.String(),
		},
	})

	return response, nil
}

func (s *WalletService) GetWallet(wristbandID uuid.UUID) (*dto.WalletResponse, error) {
	wallet, err := s.walletRepo.FindByWristbandID(wristbandID)
	if err != nil {
		return nil, ErrWalletNotFound
	}

	return &dto.WalletResponse{
		ID:       wallet.ID,
		Balance:  wallet.Balance,
		Currency: wallet.Currency,
	}, nil
}

func (s *WalletService) GetTransactions(wristbandID uuid.UUID, limit int) (*dto.TransactionListResponse, error) {
	transactions, err := s.transactionRepo.FindByWristbandID(wristbandID, limit)
	if err != nil {
		return nil, err
	}

	responses := make([]dto.TransactionResponse, len(transactions))
	for i, t := range transactions {
		responses[i] = dto.TransactionResponse{
			ID:            t.ID,
			Type:          string(t.Type),
			Amount:        t.Amount,
			BalanceBefore: t.BalanceBefore,
			BalanceAfter:  t.BalanceAfter,
			ReferenceID:   t.ReferenceID,
			CreatedAt:     t.CreatedAt,
		}
	}

	return &dto.TransactionListResponse{
		Transactions: responses,
		Total:        int64(len(responses)),
	}, nil
}

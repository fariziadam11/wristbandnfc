package handler

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/gbs/nfc-wristband-backend/internal/dto"
	"github.com/gbs/nfc-wristband-backend/internal/middleware"
	"github.com/gbs/nfc-wristband-backend/internal/service"
	"github.com/gbs/nfc-wristband-backend/pkg/utils"
)

type WalletHandler struct {
	walletService *service.WalletService
}

func NewWalletHandler(walletService *service.WalletService) *WalletHandler {
	return &WalletHandler{walletService: walletService}
}

// Topup handles wallet top up
// @Summary Top Up Wallet
// @Description Add balance to wallet
// @Tags wallet
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param request body dto.TopupRequest true "Top up data"
// @Success 200 {object} utils.Response
// @Failure 400 {object} utils.Response
// @Router /wallet/topup [post]
func (h *WalletHandler) Topup(c *gin.Context) {
	userID, ok := middleware.GetUserID(c)
	if !ok {
		utils.Unauthorized(c, "User not found in context")
		return
	}

	var req dto.TopupRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.BadRequest(c, "Invalid request: "+err.Error())
		return
	}

	result, err := h.walletService.Topup(req, userID)
	if err != nil {
		utils.BadRequest(c, err.Error())
		return
	}

	utils.Success(c, http.StatusOK, result)
}

// Payment handles payment
// @Summary Process Payment
// @Description Deduct balance from wallet
// @Tags payments
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param request body dto.PaymentRequest true "Payment data"
// @Success 200 {object} utils.Response
// @Failure 400 {object} utils.Response
// @Router /payments [post]
func (h *WalletHandler) Payment(c *gin.Context) {
	userID, ok := middleware.GetUserID(c)
	if !ok {
		utils.Unauthorized(c, "User not found in context")
		return
	}

	var req dto.PaymentRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.BadRequest(c, "Invalid request: "+err.Error())
		return
	}

	result, err := h.walletService.Payment(req, userID)
	if err != nil {
		if err == service.ErrInsufficientBalance {
			c.JSON(http.StatusBadRequest, gin.H{
				"success": false,
				"status":  "failed",
				"error":   "insufficient_balance",
			})
			return
		}
		utils.NotFound(c, err.Error())
		return
	}

	utils.Success(c, http.StatusOK, result)
}

// GetWallet handles getting wallet by wristband ID
// @Summary Get Wallet
// @Description Get wallet by wristband ID
// @Tags wallet
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param id path string true "Wristband ID"
// @Success 200 {object} utils.Response
// @Failure 404 {object} utils.Response
// @Router /wallet/{id} [get]
func (h *WalletHandler) GetWallet(c *gin.Context) {
	idStr := c.Param("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		utils.BadRequest(c, "Invalid wristband ID")
		return
	}

	wallet, err := h.walletService.GetWallet(id)
	if err != nil {
		utils.NotFound(c, "Wallet not found")
		return
	}

	utils.Success(c, http.StatusOK, wallet)
}

// GetTransactions handles getting transaction history
// @Summary Get Transactions
// @Description Get transaction history by wristband ID
// @Tags transactions
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param wristband_id query string false "Wristband ID"
// @Success 200 {object} utils.Response
// @Failure 400 {object} utils.Response
// @Router /transactions [get]
func (h *WalletHandler) GetTransactions(c *gin.Context) {
	idStr := c.Query("wristband_id")
	if idStr == "" {
		utils.BadRequest(c, "wristband_id is required")
		return
	}

	id, err := uuid.Parse(idStr)
	if err != nil {
		utils.BadRequest(c, "Invalid wristband ID")
		return
	}

	transactions, err := h.walletService.GetTransactions(id, 50)
	if err != nil {
		utils.InternalError(c, "Failed to get transactions")
		return
	}

	utils.Success(c, http.StatusOK, transactions)
}

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

type WristbandHandler struct {
	wristbandService *service.WristbandService
}

func NewWristbandHandler(wristbandService *service.WristbandService) *WristbandHandler {
	return &WristbandHandler{wristbandService: wristbandService}
}

// Register handles wristband registration
// @Summary Register Wristband
// @Description Register a new wristband with customer
// @Tags wristbands
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param request body dto.RegisterWristbandRequest true "Registration data"
// @Success 201 {object} utils.Response
// @Failure 400 {object} utils.Response
// @Router /wristbands/register [post]
func (h *WristbandHandler) Register(c *gin.Context) {
	userID, ok := middleware.GetUserID(c)
	if !ok {
		utils.Unauthorized(c, "User not found in context")
		return
	}

	var req dto.RegisterWristbandRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.BadRequest(c, "Invalid request: "+err.Error())
		return
	}

	result, err := h.wristbandService.Register(req, userID)
	if err != nil {
		utils.BadRequest(c, err.Error())
		return
	}

	utils.Success(c, http.StatusCreated, result)
}

// Write handles NFC write operation
// @Summary Write NFC
// @Description Write token to NFC tag
// @Tags wristbands
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param request body dto.WriteWristbandRequest true "UID to write"
// @Success 200 {object} utils.Response
// @Failure 400 {object} utils.Response
// @Failure 404 {object} utils.Response
// @Router /wristbands/write [post]
func (h *WristbandHandler) Write(c *gin.Context) {
	var req dto.WriteWristbandRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.BadRequest(c, "Invalid request: "+err.Error())
		return
	}

	result, err := h.wristbandService.WriteNFC(req.UID)
	if err != nil {
		utils.NotFound(c, "Wristband not found")
		return
	}

	utils.Success(c, http.StatusOK, result)
}

// Scan handles wristband scanning
// @Summary Scan Wristband
// @Description Scan wristband to get customer profile and wallet info
// @Tags wristbands
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param request body dto.ScanWristbandRequest true "UID and token"
// @Success 200 {object} utils.Response
// @Failure 400 {object} utils.Response
// @Failure 404 {object} utils.Response
// @Router /wristbands/scan [post]
func (h *WristbandHandler) Scan(c *gin.Context) {
	var req dto.ScanWristbandRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.BadRequest(c, "Invalid request: "+err.Error())
		return
	}

	result, err := h.wristbandService.Scan(req.UID, req.Token)
	if err != nil {
		utils.NotFound(c, err.Error())
		return
	}

	utils.Success(c, http.StatusOK, result)
}

// GetWristband handles getting wristband by ID
// @Summary Get Wristband
// @Description Get wristband by ID
// @Tags wristbands
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param id path string true "Wristband ID"
// @Success 200 {object} utils.Response
// @Failure 404 {object} utils.Response
// @Router /wristbands/{id} [get]
func (h *WristbandHandler) GetWristband(c *gin.Context) {
	idStr := c.Param("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		utils.BadRequest(c, "Invalid wristband ID")
		return
	}

	wristband, err := h.wristbandService.GetByWristbandID(id)
	if err != nil {
		utils.NotFound(c, "Wristband not found")
		return
	}

	utils.Success(c, http.StatusOK, wristband)
}

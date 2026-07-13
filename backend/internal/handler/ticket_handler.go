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

type TicketHandler struct {
	ticketService *service.TicketService
}

func NewTicketHandler(ticketService *service.TicketService) *TicketHandler {
	return &TicketHandler{ticketService: ticketService}
}

// Validate handles ticket validation
// @Summary Validate Ticket
// @Description Validate a ticket via wristband
// @Tags tickets
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param request body dto.ValidateTicketRequest true "UID and token"
// @Success 200 {object} utils.Response
// @Failure 400 {object} utils.Response
// @Router /tickets/validate [post]
func (h *TicketHandler) Validate(c *gin.Context) {
	userID, ok := middleware.GetUserID(c)
	if !ok {
		utils.Unauthorized(c, "User not found in context")
		return
	}

	var req dto.ValidateTicketRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.BadRequest(c, "Invalid request: "+err.Error())
		return
	}

	result, err := h.ticketService.Validate(req, userID)
	if err != nil {
		if err == service.ErrTicketNotFound {
			utils.NotFound(c, "Wristband not found")
			return
		}
		// Return the response anyway for ticket validation
		utils.Success(c, http.StatusOK, result)
		return
	}

	utils.Success(c, http.StatusOK, result)
}

// GetTickets handles getting tickets by wristband ID
// @Summary Get Tickets
// @Description Get tickets for a wristband
// @Tags tickets
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param id path string true "Wristband ID"
// @Success 200 {object} utils.Response
// @Failure 400 {object} utils.Response
// @Router /tickets/{id} [get]
func (h *TicketHandler) GetTickets(c *gin.Context) {
	idStr := c.Param("id")
	id, err := uuid.Parse(idStr)
	if err != nil {
		utils.BadRequest(c, "Invalid wristband ID")
		return
	}

	tickets, err := h.ticketService.GetTickets(id)
	if err != nil {
		utils.InternalError(c, "Failed to get tickets")
		return
	}

	utils.Success(c, http.StatusOK, tickets)
}

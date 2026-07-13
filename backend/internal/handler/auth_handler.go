package handler

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/gbs/nfc-wristband-backend/internal/dto"
	"github.com/gbs/nfc-wristband-backend/internal/middleware"
	"github.com/gbs/nfc-wristband-backend/internal/service"
	"github.com/gbs/nfc-wristband-backend/pkg/utils"
)

type AuthHandler struct {
	authService *service.AuthService
}

func NewAuthHandler(authService *service.AuthService) *AuthHandler {
	return &AuthHandler{authService: authService}
}

// Login handles user login
// @Summary Login
// @Description Authenticate user and return JWT token
// @Tags auth
// @Accept json
// @Produce json
// @Param request body dto.LoginRequest true "Login credentials"
// @Success 200 {object} utils.Response
// @Failure 400 {object} utils.Response
// @Failure 401 {object} utils.Response
// @Router /auth/login [post]
func (h *AuthHandler) Login(c *gin.Context) {
	var req dto.LoginRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.BadRequest(c, "Invalid request: "+err.Error())
		return
	}

	result, err := h.authService.Login(req)
	if err != nil {
		if err == service.ErrInvalidCredentials {
			utils.Unauthorized(c, "Invalid email or password")
			return
		}
		utils.InternalError(c, "Login failed")
		return
	}

	utils.Success(c, http.StatusOK, result)
}

// GetProfile returns the current user's profile
// @Summary Get Profile
// @Description Get current authenticated user's profile
// @Tags profile
// @Accept json
// @Produce json
// @Security BearerAuth
// @Success 200 {object} utils.Response
// @Failure 401 {object} utils.Response
// @Failure 404 {object} utils.Response
// @Router /profile/me [get]
func (h *AuthHandler) GetProfile(c *gin.Context) {
	userID, ok := middleware.GetUserID(c)
	if !ok {
		utils.Unauthorized(c, "User not found in context")
		return
	}

	profile, err := h.authService.GetProfile(userID)
	if err != nil {
		utils.NotFound(c, "User not found")
		return
	}

	utils.Success(c, http.StatusOK, profile)
}

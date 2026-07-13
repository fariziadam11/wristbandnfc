package utils

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

// Response represents a standard API response
type Response struct {
	Success bool        `json:"success"`
	Data    interface{} `json:"data,omitempty"`
	Error   string      `json:"error,omitempty"`
	Code    string      `json:"code,omitempty"`
}

// Success sends a success response
func Success(c *gin.Context, statusCode int, data interface{}) {
	c.JSON(statusCode, Response{
		Success: true,
		Data:    data,
	})
}

// Error sends an error response
func Error(c *gin.Context, statusCode int, message string, code string) {
	c.JSON(statusCode, Response{
		Success: false,
		Error:   message,
		Code:    code,
	})
}

// BadRequest sends a 400 error response
func BadRequest(c *gin.Context, message string) {
	Error(c, http.StatusBadRequest, message, "BAD_REQUEST")
}

// Unauthorized sends a 401 error response
func Unauthorized(c *gin.Context, message string) {
	Error(c, http.StatusUnauthorized, message, "UNAUTHORIZED")
}

// Forbidden sends a 403 error response
func Forbidden(c *gin.Context, message string) {
	Error(c, http.StatusForbidden, message, "FORBIDDEN")
}

// NotFound sends a 404 error response
func NotFound(c *gin.Context, message string) {
	Error(c, http.StatusNotFound, message, "NOT_FOUND")
}

// InternalError sends a 500 error response
func InternalError(c *gin.Context, message string) {
	Error(c, http.StatusInternalServerError, message, "INTERNAL_ERROR")
}

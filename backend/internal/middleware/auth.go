package middleware

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
	"github.com/gbs/nfc-wristband-backend/internal/config"
	"github.com/gbs/nfc-wristband-backend/pkg/utils"
)

const (
	AuthorizationHeader = "Authorization"
	BearerPrefix       = "Bearer "
	UserIDKey          = "user_id"
	UserEmailKey       = "user_email"
	UserRoleKey        = "user_role"
)

// JWTAuth returns a JWT authentication middleware
func JWTAuth(cfg *config.Config) gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader(AuthorizationHeader)
		if authHeader == "" {
			utils.Unauthorized(c, "Authorization header required")
			c.Abort()
			return
		}

		if !strings.HasPrefix(authHeader, BearerPrefix) {
			utils.Unauthorized(c, "Invalid authorization header format")
			c.Abort()
			return
		}

		tokenString := strings.TrimPrefix(authHeader, BearerPrefix)

		token, err := jwt.ParseWithClaims(tokenString, &utils.JWTClaims{}, func(token *jwt.Token) (interface{}, error) {
			return []byte(cfg.JWTSecret), nil
		})

		if err != nil {
			utils.Unauthorized(c, "Invalid or expired token")
			c.Abort()
			return
		}

		claims, ok := token.Claims.(*utils.JWTClaims)
		if !ok || !token.Valid {
			utils.Unauthorized(c, "Invalid token claims")
			c.Abort()
			return
		}

		// Parse user ID
		userID, err := uuid.Parse(claims.UserID)
		if err != nil {
			utils.Unauthorized(c, "Invalid user ID in token")
			c.Abort()
			return
		}

		// Set user info in context
		c.Set(UserIDKey, userID)
		c.Set(UserEmailKey, claims.Email)
		c.Set(UserRoleKey, claims.Role)

		c.Next()
	}
}

// GetUserID extracts user ID from context
func GetUserID(c *gin.Context) (uuid.UUID, bool) {
	userID, exists := c.Get(UserIDKey)
	if !exists {
		return uuid.Nil, false
	}
	return userID.(uuid.UUID), true
}

// GetUserEmail extracts user email from context
func GetUserEmail(c *gin.Context) (string, bool) {
	email, exists := c.Get(UserEmailKey)
	if !exists {
		return "", false
	}
	return email.(string), true
}

// GetUserRole extracts user role from context
func GetUserRole(c *gin.Context) (string, bool) {
	role, exists := c.Get(UserRoleKey)
	if !exists {
		return "", false
	}
	return role.(string), true
}

// RequireRole returns a middleware that requires specific roles
func RequireRole(roles ...string) gin.HandlerFunc {
	return func(c *gin.Context) {
		role, exists := GetUserRole(c)
		if !exists {
			utils.Unauthorized(c, "User role not found")
			c.Abort()
			return
		}

		for _, r := range roles {
			if role == r {
				c.Next()
				return
			}
		}

		utils.Forbidden(c, "Insufficient permissions")
		c.Abort()
	}
}

// Logger returns a logging middleware
func Logger() gin.HandlerFunc {
	return gin.LoggerWithFormatter(func(param gin.LogFormatterParams) string {
		return param.TimeStamp.Format("2006/01/02 - 15:04:05") +
			" | " + param.Method +
			" | " + param.Path +
			" | " + param.ClientIP +
			" | " + param.Latency.String() +
			" | " + string(rune(param.StatusCode)) +
			" | " + param.ErrorMessage + "\n"
	})
}

// Recovery returns a recovery middleware
func Recovery() gin.HandlerFunc {
	return gin.Recovery()
}

// CORS returns a CORS middleware
func CORS() gin.HandlerFunc {
	return func(c *gin.Context) {
		c.Writer.Header().Set("Access-Control-Allow-Origin", "*")
		c.Writer.Header().Set("Access-Control-Allow-Credentials", "true")
		c.Writer.Header().Set("Access-Control-Allow-Headers", "Content-Type, Content-Length, Accept-Encoding, X-CSRF-Token, Authorization, accept, origin, Cache-Control, X-Requested-With")
		c.Writer.Header().Set("Access-Control-Allow-Methods", "POST, OPTIONS, GET, PUT, DELETE, PATCH")

		if c.Request.Method == http.MethodOptions {
			c.AbortWithStatus(http.StatusNoContent)
			return
		}

		c.Next()
	}
}

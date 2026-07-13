package router

import (
	"github.com/gin-gonic/gin"
	"github.com/gbs/nfc-wristband-backend/internal/config"
	"github.com/gbs/nfc-wristband-backend/internal/handler"
	"github.com/gbs/nfc-wristband-backend/internal/middleware"
)

type Router struct {
	engine           *gin.Engine
	cfg              *config.Config
	authHandler      *handler.AuthHandler
	wristbandHandler *handler.WristbandHandler
	walletHandler    *handler.WalletHandler
	ticketHandler    *handler.TicketHandler
}

func NewRouter(
	cfg *config.Config,
	authHandler *handler.AuthHandler,
	wristbandHandler *handler.WristbandHandler,
	walletHandler *handler.WalletHandler,
	ticketHandler *handler.TicketHandler,
) *Router {
	return &Router{
		engine:           gin.New(),
		cfg:              cfg,
		authHandler:      authHandler,
		wristbandHandler: wristbandHandler,
		walletHandler:    walletHandler,
		ticketHandler:    ticketHandler,
	}
}

func (r *Router) Setup() *gin.Engine {
	// Middleware
	r.engine.Use(gin.Logger())
	r.engine.Use(gin.Recovery())
	r.engine.Use(middleware.CORS())

	// Health check
	r.engine.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{"status": "ok"})
	})

	// API v1
	v1 := r.engine.Group("/api/v1")
	{
		// Public routes
		auth := v1.Group("/auth")
		{
			auth.POST("/login", r.authHandler.Login)
		}

		// Protected routes
		protected := v1.Group("")
		protected.Use(middleware.JWTAuth(r.cfg))
		{
			// Profile
			protected.GET("/profile/me", r.authHandler.GetProfile)

			// Wristbands
			wristbands := protected.Group("/wristbands")
			{
				wristbands.POST("/register", r.wristbandHandler.Register)
				wristbands.POST("/write", r.wristbandHandler.Write)
				wristbands.POST("/scan", r.wristbandHandler.Scan)
				wristbands.GET("/:id", r.wristbandHandler.GetWristband)
			}

			// Wallet
			wallet := protected.Group("/wallet")
			{
				wallet.POST("/topup", r.walletHandler.Topup)
				wallet.GET("/:id", r.walletHandler.GetWallet)
			}

			// Payments
			protected.POST("/payments", r.walletHandler.Payment)

			// Transactions
			protected.GET("/transactions", r.walletHandler.GetTransactions)

			// Tickets
			tickets := protected.Group("/tickets")
			{
				tickets.POST("/validate", r.ticketHandler.Validate)
				tickets.GET("/:id", r.ticketHandler.GetTickets)
			}
		}
	}

	return r.engine
}

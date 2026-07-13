package main

import (
	"fmt"
	"log"

	"github.com/gbs/nfc-wristband-backend/internal/config"
	"github.com/gbs/nfc-wristband-backend/internal/handler"
	"github.com/gbs/nfc-wristband-backend/internal/model"
	"github.com/gbs/nfc-wristband-backend/internal/repository"
	"github.com/gbs/nfc-wristband-backend/internal/router"
	"github.com/gbs/nfc-wristband-backend/internal/service"
	"github.com/gbs/nfc-wristband-backend/pkg/utils"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

func main() {
	// Load config
	cfg := config.Load()

	// Connect to database
	db, err := connectDB(cfg)
	if err != nil {
		log.Fatalf("Failed to connect to database: %v", err)
	}

	// Auto migrate
	if err := db.AutoMigrate(
		&model.User{},
		&model.Wristband{},
		&model.Wallet{},
		&model.Transaction{},
		&model.Ticket{},
		&model.AuditLog{},
	); err != nil {
		log.Fatalf("Failed to migrate database: %v", err)
	}

	// Seed data
	seedData(db, cfg)

	// Initialize repositories
	userRepo := repository.NewUserRepository(db)
	wristbandRepo := repository.NewWristbandRepository(db)
	walletRepo := repository.NewWalletRepository(db)
	transactionRepo := repository.NewTransactionRepository(db)
	ticketRepo := repository.NewTicketRepository(db)
	auditRepo := repository.NewAuditLogRepository(db)

	// Initialize services
	authService := service.NewAuthService(userRepo, cfg)
	wristbandService := service.NewWristbandService(wristbandRepo, userRepo, walletRepo, ticketRepo, auditRepo)
	walletService := service.NewWalletService(db, walletRepo, wristbandRepo, transactionRepo, auditRepo)
	ticketService := service.NewTicketService(db, wristbandRepo, ticketRepo, auditRepo)

	// Initialize handlers
	authHandler := handler.NewAuthHandler(authService)
	wristbandHandler := handler.NewWristbandHandler(wristbandService)
	walletHandler := handler.NewWalletHandler(walletService)
	ticketHandler := handler.NewTicketHandler(ticketService)

	// Setup router
	r := router.NewRouter(cfg, authHandler, wristbandHandler, walletHandler, ticketHandler)
	engine := r.Setup()

	// Start server
	addr := fmt.Sprintf(":%s", cfg.ServerPort)
	log.Printf("Starting server on %s", addr)
	if err := engine.Run(addr); err != nil {
		log.Fatalf("Failed to start server: %v", err)
	}
}

func connectDB(cfg *config.Config) (*gorm.DB, error) {
	dsn := fmt.Sprintf(
		"host=%s user=%s password=%s dbname=%s port=%s sslmode=disable TimeZone=Asia/Jakarta",
		cfg.DBHost,
		cfg.DBUser,
		cfg.DBPassword,
		cfg.DBName,
		cfg.DBPort,
	)

	db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Info),
	})
	if err != nil {
		return nil, err
	}

	return db, nil
}

func seedData(db *gorm.DB, cfg *config.Config) {
	// Check if petugas already exists
	var count int64
	db.Model(&model.User{}).Where("role = ?", model.RolePetugas).Count(&count)
	if count > 0 {
		log.Println("Seed data already exists, skipping...")
		return
	}

	log.Println("Seeding initial data...")

	// Create petugas users
	petugasUsers := []struct {
		name     string
		email    string
		password string
	}{
		{"Petugas 1", "petugas1@demo.com", "secret123"},
		{"Petugas 2", "petugas2@demo.com", "secret123"},
		{"Admin", "admin@demo.com", "admin123"},
	}

	for _, p := range petugasUsers {
		hashedPassword, _ := utils.HashPassword(p.password)
		user := &model.User{
			Name:         p.name,
			Email:        p.email,
			PasswordHash: hashedPassword,
			Role:         model.RolePetugas,
		}
		if err := db.Create(user).Error; err != nil {
			log.Printf("Failed to create user %s: %v", p.email, err)
		}
	}

	// Create sample customers with wristbands
	sampleCustomers := []struct {
		name    string
		balance int64
	}{
		{"Budi Santoso", 150000},
		{"Ani Wijaya", 75000},
		{"Dewi Lestari", 200000},
	}

	for _, c := range sampleCustomers {
		// Create customer
		hashedPassword, _ := utils.HashPassword("customer123")
		customer := &model.User{
			Name:         c.name,
			Email:        c.name + "@nfc.local",
			PasswordHash: hashedPassword,
			Role:         model.RoleCustomer,
		}
		if err := db.Create(customer).Error; err != nil {
			log.Printf("Failed to create customer %s: %v", c.name, err)
			continue
		}

		// Get first petugas for registration
		var petugas model.User
		db.Where("role = ?", model.RolePetugas).First(&petugas)

		// Generate UID and Token
		uid, _ := utils.GenerateToken(4)
		token, _ := utils.GenerateShortToken()

		// Create wristband
		wristband := &model.Wristband{
			UID:          uid,
			Token:        token,
			CustomerID:   customer.ID,
			RegisteredBy: petugas.ID,
			Status:       model.StatusActive,
			IsWritten:    true,
		}
		if err := db.Create(wristband).Error; err != nil {
			log.Printf("Failed to create wristband for %s: %v", c.name, err)
			continue
		}

		// Create wallet with balance
		wallet := &model.Wallet{
			WristbandID: wristband.ID,
			Balance:     c.balance,
			Currency:    "IDR",
		}
		if err := db.Create(wallet).Error; err != nil {
			log.Printf("Failed to create wallet for %s: %v", c.name, err)
			continue
		}

		// Create sample ticket
		ticket := &model.Ticket{
			WristbandID: wristband.ID,
			TicketType:  "1-day-pass",
			Description: "Valid for 1 day entry",
			Price:       50000,
			Status:      model.TicketValid,
		}
		if err := db.Create(ticket).Error; err != nil {
			log.Printf("Failed to create ticket for %s: %v", c.name, err)
		}

		log.Printf("Created customer: %s, UID: %s, Token: %s", c.name, uid, token)
	}

	log.Println("Seed data completed!")
}

package model

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// UserRole represents the role of a user
type UserRole string

const (
	RolePetugas  UserRole = "petugas"
	RoleCustomer UserRole = "customer"
)

// User represents a user (petugas or customer)
type User struct {
	ID           uuid.UUID  `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	Name         string     `gorm:"type:varchar(100);not null" json:"name"`
	Email        string     `gorm:"type:varchar(100);uniqueIndex;not null" json:"email"`
	PasswordHash string     `gorm:"type:varchar(255);not null" json:"-"`
	Role         UserRole   `gorm:"type:varchar(20);not null;default:'customer'" json:"role"`
	CreatedAt    time.Time  `gorm:"default:CURRENT_TIMESTAMP" json:"created_at"`
	UpdatedAt    time.Time  `gorm:"default:CURRENT_TIMESTAMP" json:"updated_at"`
	DeletedAt    gorm.DeletedAt `gorm:"index" json:"-"`

	// Relations
	Wristbands  []Wristband `gorm:"foreignKey:CustomerID" json:"wristbands,omitempty"`
	RegisteredBy []Wristband `gorm:"foreignKey:RegisteredBy" json:"registered_by,omitempty"`
	AuditLogs   []AuditLog  `gorm:"foreignKey:ActorUserID" json:"audit_logs,omitempty"`
}

func (User) TableName() string {
	return "users"
}

// WristbandStatus represents the status of a wristband
type WristbandStatus string

const (
	StatusActive    WristbandStatus = "active"
	StatusInactive  WristbandStatus = "inactive"
	StatusLost      WristbandStatus = "lost"
)

// Wristband represents an NFC wristband
type Wristband struct {
	ID           uuid.UUID       `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	UID          string          `gorm:"type:varchar(50);uniqueIndex;not null" json:"uid"` // Physical NFC chip UID
	Token        string          `gorm:"type:varchar(50);uniqueIndex;not null" json:"token"` // Random token written to NDEF
	CustomerID   uuid.UUID       `gorm:"type:uuid;not null" json:"customer_id"`
	Customer     User            `gorm:"foreignKey:CustomerID" json:"customer,omitempty"`
	RegisteredBy uuid.UUID       `gorm:"type:uuid;not null" json:"registered_by"`
	RegisteredByUser User         `gorm:"foreignKey:RegisteredBy" json:"registered_by_user,omitempty"`
	Status       WristbandStatus `gorm:"type:varchar(20);default:'active'" json:"status"`
	IsWritten    bool            `gorm:"default:false" json:"is_written"` // Whether token has been written to NFC tag
	RegisteredAt time.Time       `gorm:"default:CURRENT_TIMESTAMP" json:"registered_at"`

	// Relations - use pointer to avoid recursive type
	Wallet   *Wallet   `gorm:"foreignKey:WristbandID" json:"wallet,omitempty"`
	Tickets  []Ticket  `gorm:"foreignKey:WristbandID" json:"tickets,omitempty"`
}

func (Wristband) TableName() string {
	return "wristbands"
}

// Wallet represents a user's wallet
type Wallet struct {
	ID         uuid.UUID `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	WristbandID uuid.UUID `gorm:"type:uuid;uniqueIndex;not null" json:"wristband_id"`
	Balance    int64     `gorm:"type:bigint;default:0" json:"balance"` // Balance in cents/smallest unit
	Currency   string    `gorm:"type:varchar(3);default:'IDR'" json:"currency"`
	UpdatedAt  time.Time `gorm:"default:CURRENT_TIMESTAMP" json:"updated_at"`
	CreatedAt  time.Time `gorm:"default:CURRENT_TIMESTAMP" json:"created_at"`

	// Relations
	Transactions []Transaction `gorm:"foreignKey:WalletID" json:"transactions,omitempty"`
}

func (Wallet) TableName() string {
	return "wallets"
}

// TransactionType represents the type of transaction
type TransactionType string

const (
	TransactionPayment TransactionType = "payment"
	TransactionTopup  TransactionType = "topup"
	TransactionRefund TransactionType = "refund"
)

// TransactionStatus represents the status of a transaction
type TransactionStatus string

const (
	TransactionSuccess TransactionStatus = "success"
	TransactionFailed TransactionStatus = "failed"
)

// Transaction represents a financial transaction
type Transaction struct {
	ID            uuid.UUID        `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	WalletID      uuid.UUID        `gorm:"type:uuid;not null" json:"wallet_id"`
	Type          TransactionType  `gorm:"type:varchar(20);not null" json:"type"`
	Amount        int64            `gorm:"type:bigint;not null" json:"amount"`
	BalanceBefore int64            `gorm:"type:bigint;not null" json:"balance_before"`
	BalanceAfter  int64            `gorm:"type:bigint;not null" json:"balance_after"`
	ReferenceID   string           `gorm:"type:varchar(100)" json:"reference_id"` // External reference
	PetugasID     uuid.UUID        `gorm:"type:uuid;not null" json:"petugas_id"`   // Who processed
	Petugas       User             `gorm:"foreignKey:PetugasID" json:"petugas,omitempty"`
	CreatedAt     time.Time        `gorm:"default:CURRENT_TIMESTAMP" json:"created_at"`
}

func (Transaction) TableName() string {
	return "transactions"
}

// TicketStatus represents the status of a ticket
type TicketStatus string

const (
	TicketValid   TicketStatus = "valid"
	TicketUsed    TicketStatus = "used"
	TicketExpired TicketStatus = "expired"
)

// Ticket represents a ticket associated with a wristband
type Ticket struct {
	ID           uuid.UUID     `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	WristbandID  uuid.UUID     `gorm:"type:uuid;not null" json:"wristband_id"`
	TicketType   string        `gorm:"type:varchar(50);not null" json:"ticket_type"` // e.g., "1-day-pass", "vip"
	Description  string        `gorm:"type:text" json:"description"`
	Price        int64         `gorm:"type:bigint;default:0" json:"price"` // Price in cents
	Status       TicketStatus  `gorm:"type:varchar(20);default:'valid'" json:"status"`
	ValidFrom    time.Time     `gorm:"default:CURRENT_TIMESTAMP" json:"valid_from"`
	ValidUntil   *time.Time    `json:"valid_until,omitempty"`
	UsedAt       *time.Time    `json:"used_at,omitempty"`
	UsedBy       *uuid.UUID    `gorm:"type:uuid" json:"used_by,omitempty"`
	CreatedAt    time.Time     `gorm:"default:CURRENT_TIMESTAMP" json:"created_at"`
}

func (Ticket) TableName() string {
	return "tickets"
}

// AuditLog represents an audit log entry
type AuditLog struct {
	ID         uuid.UUID              `gorm:"type:uuid;primary_key;default:gen_random_uuid()" json:"id"`
	ActorUserID uuid.UUID             `gorm:"type:uuid;not null" json:"actor_user_id"`
	Actor      User                   `gorm:"foreignKey:ActorUserID" json:"actor,omitempty"`
	Action     string                 `gorm:"type:varchar(50);not null" json:"action"`
	Entity     string                 `gorm:"type:varchar(50);not null" json:"entity"` // e.g., "wristband", "wallet"
	EntityID   string                 `gorm:"type:varchar(100)" json:"entity_id"`
	Metadata   map[string]interface{} `gorm:"type:jsonb" json:"metadata,omitempty"`
	IPAddress  string                 `gorm:"type:varchar(45)" json:"ip_address"`
	CreatedAt  time.Time              `gorm:"default:CURRENT_TIMESTAMP" json:"created_at"`
}

func (AuditLog) TableName() string {
	return "audit_logs"
}

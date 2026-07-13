package dto

import (
	"time"

	"github.com/google/uuid"
)

// ==================== AUTH DTOs ====================

// LoginRequest represents the login request body
type LoginRequest struct {
	Email    string `json:"email" binding:"required,email"`
	Password string `json:"password" binding:"required,min=6"`
}

// LoginResponse represents the login response
type LoginResponse struct {
	Token string       `json:"token"`
	User  UserResponse  `json:"user"`
}

// ==================== USER DTOs ====================

// UserResponse represents a user in API responses
type UserResponse struct {
	ID        uuid.UUID `json:"id"`
	Name      string    `json:"name"`
	Email     string    `json:"email"`
	Role      string    `json:"role"`
	CreatedAt time.Time `json:"created_at"`
}

// ==================== WRISTBAND DTOs ====================

// RegisterWristbandRequest represents the request to register a new wristband
type RegisterWristbandRequest struct {
	CustomerName string `json:"customer_name" binding:"required,min=2"`
	CustomerEmail string `json:"customer_email" binding:"omitempty,email"`
}

// RegisterWristbandResponse represents the response after registering a wristband
type RegisterWristbandResponse struct {
	ID           uuid.UUID `json:"id"`
	UID          string    `json:"uid"`
	Token        string    `json:"token"`
	CustomerID   uuid.UUID `json:"customer_id"`
	CustomerName string    `json:"customer_name"`
	RegisteredBy uuid.UUID `json:"registered_by"`
}

// WriteWristbandRequest represents the request to write NFC token
type WriteWristbandRequest struct {
	UID string `json:"uid" binding:"required"`
}

// WriteWristbandResponse represents the response after writing NFC
type WriteWristbandResponse struct {
	UID     string `json:"uid"`
	Token   string `json:"token"`
	Message string `json:"message"`
}

// ScanWristbandRequest represents the request to scan a wristband
type ScanWristbandRequest struct {
	UID   string `json:"uid" binding:"required"`
	Token string `json:"token" binding:"required"`
}

// ScanWristbandResponse represents the response after scanning a wristband
type ScanWristbandResponse struct {
	WristbandID   uuid.UUID        `json:"wristband_id"`
	UID           string           `json:"uid"`
	Token         string           `json:"token"`
	Status        string           `json:"status"`
	Customer      CustomerResponse `json:"customer"`
	Wallet        WalletResponse   `json:"wallet"`
	ActiveTickets []TicketResponse `json:"active_tickets,omitempty"`
}

// CustomerResponse represents customer info in responses
type CustomerResponse struct {
	ID   uuid.UUID `json:"id"`
	Name string    `json:"name"`
}

// ==================== WALLET DTOs ====================

// WalletResponse represents wallet info in responses
type WalletResponse struct {
	ID       uuid.UUID `json:"id"`
	Balance  int64     `json:"balance"`
	Currency string    `json:"currency"`
}

// TopupRequest represents the request to top up wallet
type TopupRequest struct {
	UID    string `json:"uid" binding:"required"`
	Token  string `json:"token" binding:"required"`
	Amount int64  `json:"amount" binding:"required,gt=0"`
}

// TopupResponse represents the response after top up
type TopupResponse struct {
	TransactionID   uuid.UUID `json:"transaction_id"`
	WristbandID     uuid.UUID `json:"wristband_id"`
	PreviousBalance int64     `json:"previous_balance"`
	Amount          int64     `json:"amount"`
	NewBalance      int64     `json:"new_balance"`
}

// GetWalletResponse represents the response for getting wallet
type GetWalletResponse struct {
	ID       uuid.UUID `json:"id"`
	Balance  int64     `json:"balance"`
	Currency string    `json:"currency"`
	UID      string    `json:"uid"`
}

// ==================== PAYMENT DTOs ====================

// PaymentRequest represents the request to make a payment
type PaymentRequest struct {
	UID         string `json:"uid" binding:"required"`
	Token       string `json:"token" binding:"required"`
	Amount      int64  `json:"amount" binding:"required,gt=0"`
	ReferenceID string `json:"reference_id"`
}

// PaymentResponse represents the response after payment
type PaymentResponse struct {
	TransactionID   uuid.UUID `json:"transaction_id"`
	Status          string    `json:"status"`
	PreviousBalance int64     `json:"previous_balance"`
	Amount          int64     `json:"amount"`
	NewBalance      int64     `json:"new_balance"`
}

// PaymentErrorResponse represents payment error
type PaymentErrorResponse struct {
	Status          string `json:"status"`
	Error           string `json:"error"`
	CurrentBalance  int64  `json:"current_balance,omitempty"`
}

// ==================== TICKET DTOs ====================

// TicketResponse represents ticket info in responses
type TicketResponse struct {
	ID          uuid.UUID  `json:"id"`
	TicketType  string     `json:"ticket_type"`
	Description string     `json:"description"`
	Price       int64      `json:"price"`
	Status      string     `json:"status"`
	ValidFrom   time.Time  `json:"valid_from"`
	ValidUntil  *time.Time `json:"valid_until,omitempty"`
	UsedAt      *time.Time `json:"used_at,omitempty"`
}

// ValidateTicketRequest represents the request to validate a ticket
type ValidateTicketRequest struct {
	UID   string `json:"uid" binding:"required"`
	Token string `json:"token" binding:"required"`
}

// ValidateTicketResponse represents the response after ticket validation
type ValidateTicketResponse struct {
	Result     string    `json:"result"` // valid, already_used, expired, not_found
	TicketType string    `json:"ticket_type,omitempty"`
	ValidUntil *time.Time `json:"valid_until,omitempty"`
	UsedAt     *time.Time `json:"used_at,omitempty"`
	Message    string    `json:"message"`
}

// ==================== TRANSACTION DTOs ====================

// TransactionResponse represents transaction info in responses
type TransactionResponse struct {
	ID            uuid.UUID `json:"id"`
	Type          string    `json:"type"`
	Amount        int64     `json:"amount"`
	BalanceBefore int64     `json:"balance_before"`
	BalanceAfter  int64     `json:"balance_after"`
	ReferenceID   string    `json:"reference_id,omitempty"`
	Status        string    `json:"status"`
	CreatedAt     time.Time `json:"created_at"`
}

// TransactionListResponse represents a list of transactions
type TransactionListResponse struct {
	Transactions []TransactionResponse `json:"transactions"`
	Total        int64                `json:"total"`
}

// ==================== ERROR DTOs ====================

// ErrorResponse represents an error response
type ErrorResponse struct {
	Error   string `json:"error"`
	Code    string `json:"code,omitempty"`
	Details string `json:"details,omitempty"`
}

// ==================== GENERIC DTOs ====================

// MessageResponse represents a simple message response
type MessageResponse struct {
	Message string `json:"message"`
}

// SuccessResponse wraps successful responses
type SuccessResponse struct {
	Success bool        `json:"success"`
	Data    interface{} `json:"data,omitempty"`
}

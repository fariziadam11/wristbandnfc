package service

import (
	"errors"

	"github.com/google/uuid"
	"github.com/gbs/nfc-wristband-backend/internal/config"
	"github.com/gbs/nfc-wristband-backend/internal/dto"
	"github.com/gbs/nfc-wristband-backend/internal/repository"
	"github.com/gbs/nfc-wristband-backend/pkg/utils"
)

var (
	ErrInvalidCredentials = errors.New("invalid credentials")
	ErrUserNotFound       = errors.New("user not found")
)

type AuthService struct {
	userRepo *repository.UserRepository
	config   *config.Config
}

func NewAuthService(userRepo *repository.UserRepository, cfg *config.Config) *AuthService {
	return &AuthService{
		userRepo: userRepo,
		config:   cfg,
	}
}

func (s *AuthService) Login(req dto.LoginRequest) (*dto.LoginResponse, error) {
	user, err := s.userRepo.FindByEmail(req.Email)
	if err != nil {
		return nil, ErrInvalidCredentials
	}

	if !utils.CheckPassword(req.Password, user.PasswordHash) {
		return nil, ErrInvalidCredentials
	}

	token, err := utils.GenerateJWT(
		user.ID.String(),
		user.Email,
		string(user.Role),
		s.config.JWTSecret,
		s.config.JWTExpiration,
	)
	if err != nil {
		return nil, err
	}

	return &dto.LoginResponse{
		Token: token,
		User: dto.UserResponse{
			ID:        user.ID,
			Name:      user.Name,
			Email:     user.Email,
			Role:      string(user.Role),
			CreatedAt: user.CreatedAt,
		},
	}, nil
}

func (s *AuthService) GetProfile(userID uuid.UUID) (*dto.UserResponse, error) {
	user, err := s.userRepo.FindByID(userID)
	if err != nil {
		return nil, ErrUserNotFound
	}

	return &dto.UserResponse{
		ID:        user.ID,
		Name:      user.Name,
		Email:     user.Email,
		Role:      string(user.Role),
		CreatedAt: user.CreatedAt,
	}, nil
}

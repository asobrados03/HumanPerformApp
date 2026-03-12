# Diagrama de clases: dependencia de `AuthViewModel` con estados de UI

Este diagrama muestra **solo** la relación entre `AuthViewModel` y las clases de estado de UI que expone mediante `StateFlow`.

```mermaid
classDiagram
    direction LR
    class AuthViewModel {
          -authUseCase: AuthUseCase
          +loginState: StateFlow~LoginState~
          +registerState: StateFlow~RegisterState~
          +isChangingPassword: StateFlow~ChangePasswordState~
          +isResettingPassword: StateFlow~ResetPasswordState~
          +login(email, password)
          +register(data)
          +resetPassword(email)
          +changePassword(currentPassword, newPassword, confirmPassword, userId)
          +resetStates()
          +resetChangePasswordState()
          +resetResettingPasswordState()
    }

    class LoginState {
      <<sealed>>
      Idle
      Loading
      Success(user: LoginResponse)
      Error(message: String)
    }

    class RegisterState {
      <<sealed>>
      Idle
      Loading
      Success(message: RegisterResponse)
      Error(message: String)
      ValidationErrors(fieldErrors: Map~RegisterField,String~)
    }

    class ChangePasswordState {
      <<sealed>>
      Idle
      Loading
      Success(message: String)
      Error(message: String)
    }

    class ResetPasswordState {
      <<sealed>>
      Idle
      Loading
      Success(message: String)
      Error(message: String)
    }

    AuthViewModel --> LoginState : mantiene/expone
    AuthViewModel --> RegisterState : mantiene/expone
    AuthViewModel --> ChangePasswordState : mantiene/expone
    AuthViewModel --> ResetPasswordState : mantiene/expone
```

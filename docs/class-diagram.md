# CrediYa - UML Class Diagram

```mermaid
classDiagram
    direction TB

    class Person {
        <<abstract>>
        -int id
        -String name
        -String document
        -String email
        +getType()* String
        +describe()* String
        +toFileLine()* String
    }
    class Employee {
        -String role
        -double salary
    }
    class Client {
        -String phone
        -String address
        -List~Loan~ loans
    }
    class Loan {
        -int id
        -int clientId
        -int employeeId
        -double principal
        -double monthlyRate
        -int termMonths
        -double totalInterest
        -double totalAmount
        -double monthlyInstallment
        -double outstandingBalance
        -LocalDate startDate
        -LoanStatus status
        +getNextDueDate() LocalDate
        +getDaysOverdue(today) long
        +computeStatus(today) LoanStatus
    }
    class Payment {
        -int id
        -int loanId
        -double amount
        -LocalDate paymentDate
        -double balanceAfter
    }
    class LoanStatus {
        <<enumeration>>
        ACTIVE
        OVERDUE
        PAID
    }
    Person <|-- Employee
    Person <|-- Client
    Client "1" o-- "0..*" Loan
    Employee "1" --> "0..*" Loan : approves
    Loan "1" *-- "0..*" Payment
    Loan --> LoanStatus

    class GenericDAO~T~ {
        <<interface>>
        +insert(T) int
        +findAll() List~T~
        +findById(int) Optional~T~
    }
    class EmployeeDAO
    class ClientDAO
    class LoanDAO
    class PaymentDAO
    GenericDAO <|.. EmployeeDAO
    GenericDAO <|.. ClientDAO
    GenericDAO <|.. LoanDAO
    GenericDAO <|.. PaymentDAO

    class InterestCalculator {
        <<interface>>
        +calculateInterest(principal, rate, months) double
    }
    class SimpleInterestCalculator
    InterestCalculator <|.. SimpleInterestCalculator

    class EmployeeService
    class ClientService
    class LoanService
    class PaymentService
    class ReportService
    EmployeeService --> EmployeeDAO
    ClientService --> ClientDAO
    ClientService --> LoanDAO
    LoanService --> LoanDAO
    LoanService --> InterestCalculator
    PaymentService --> PaymentDAO
    PaymentService --> LoanDAO
    ReportService --> LoanService
    ReportService --> ClientService

    class DBConnection {
        <<singleton>>
        +getInstance() DBConnection
        +getConnection() Connection
    }
    class FileManager {
        +append() +overwrite() +readAll() +log()
    }
    EmployeeDAO ..> DBConnection
    LoanDAO ..> DBConnection
    LoanService ..> FileManager

    class Menu {
        <<abstract>>
        +run()
        #title()* String
        #options()* List
        #execute(int)*
    }
    Menu <|-- MainMenu
    Menu <|-- EmployeeMenu
    Menu <|-- ClientMenu
    Menu <|-- LoanMenu
    Menu <|-- PaymentMenu
    Menu <|-- ReportMenu
    MainMenu o-- EmployeeMenu
    MainMenu o-- ClientMenu
    MainMenu o-- LoanMenu
    MainMenu o-- PaymentMenu
    MainMenu o-- ReportMenu
    EmployeeMenu --> EmployeeService
    ClientMenu --> ClientService
    LoanMenu --> LoanService
    PaymentMenu --> PaymentService
    ReportMenu --> ReportService
```

## Layers
`view` (menus) -> `service` (business rules) -> `dao` (JDBC / SQL) -> MySQL.
`model` is shared by all layers; `util` has the Singleton connection, text-file manager and input helpers.

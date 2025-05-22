# Product Requirements Document (PRD)
## Personal Accountant - Expense Tracker Android App

### 1. Product Overview
**App Name:** Personal Accountant  
**Platform:** Android  
**Version:** 1.0 (MVP)  
**Target Audience:** Individual users who want to track their daily expenses  
**Primary Goal:** Simple expense tracking with minimal user interaction

### 2. Core Features

#### 2.1 Main Screen (Expense Entry)
- **Total Display**: Shows sum of all expenses at the very top
- **Keypad Interface**: Numeric keypad for amount entry
- **Amount Display**: Shows the current amount being entered
- **Add Button**: Saves the entered amount to database and navigates to expense list

#### 2.2 Expense List Screen
- **Total Display**: Shows sum of all expenses at the top
- **Expense List**: Displays all saved expenses with:
  - Amount
  - Smart date formatting:
    - Today's entries: "Today" + time
    - Yesterday's entries: "Yesterday" + time
    - Older entries: Full date + time
- **Add Button**: Returns to main screen for new entry

### 3. Technical Requirements

#### 3.1 Architecture
- **Database**: Room Database for local storage
- **UI Framework**: Jetpack Compose
- **Architecture Pattern**: MVVM with Clean Architecture
- **Navigation**: Compose Navigation (single Activity)
- **Dependency Injection**: Hilt
- **Language**: Kotlin

#### 3.2 Database Schema
```
Expense Entity:
- id: Primary Key (Auto-generated)
- amount: Double
- timestamp: Long (Unix timestamp)
```

#### 3.3 Navigation Architecture
- **Single Activity**: MainActivity with Jetpack Compose
- **Navigation Component**: Compose Navigation for screen transitions
- **Route Definitions**:
  - `/expense-entry` - Main Screen (Entry)
  - `/expense-list` - Expense List Screen
- **Navigation Flow**: Simple back-and-forth between two destinations

#### 3.4 Dependency Injection
- **Framework**: Hilt for dependency injection
- **Modules**:
  - DatabaseModule: Provides Room database and DAOs
  - RepositoryModule: Provides repository implementations
- **Scopes**: @Singleton for database and repository instances

### 4. User Flow

#### 4.1 Primary Flow
1. User opens app → Main Screen displayed
2. User sees total expenses at top
3. User enters amount using keypad
4. User taps "Add" button
5. Amount saved to database
6. User navigated to Expense List Screen
7. User sees all expenses with smart date formatting
8. User can tap "Add" to return to Main Screen

#### 4.2 Data Flow
1. App launches → Room database queried for total
2. Total displayed on Main Screen
3. User enters amount → stored in local state
4. Add button pressed → amount saved to Room database
5. Navigation to List Screen → Room database queried for all expenses
6. Expenses displayed with formatted dates/times

### 5. UI/UX Requirements

#### 5.1 Main Screen Layout
```
┌─────────────────────────┐
│   Total: $XXX.XX       │ ← Very top
├─────────────────────────┤
│                         │
│      Amount Display     │
│                         │
├─────────────────────────┤
│                         │
│       Keypad            │
│   [1] [2] [3]          │
│   [4] [5] [6]          │
│   [7] [8] [9]          │
│   [.] [0] [⌫]          │
│                         │
├─────────────────────────┤
│      [ADD BUTTON]       │
└─────────────────────────┘
```

#### 5.2 Expense List Screen Layout
```
┌─────────────────────────┐
│   Total: $XXX.XX       │ ← Top
├─────────────────────────┤
│ $XX.XX - Today 2:30 PM  │
│ $XX.XX - Today 10:15 AM │
│ $XX.XX - Yesterday 8:45 PM│
│ $XX.XX - Dec 15, 3:20 PM│
│ $XX.XX - Dec 14, 11:30 AM│
│        ...              │
├─────────────────────────┤
│      [ADD BUTTON]       │
└─────────────────────────┘
```

### 6. Constraints and Limitations

#### 6.1 Scope Limitations (V1.0)
- No expense categories
- No expense editing/deletion
- No expense search/filtering
- No data export features
- No user authentication
- No cloud sync
- No expense descriptions/notes

#### 6.2 Technical Constraints
- Android only (no iOS version)
- Local storage only (Room database)
- No network connectivity required
- Minimum Android SDK: 24 (Android 7.0)

### 7. Success Metrics
- App successfully saves expenses to local database
- Total calculation displays correctly
- Smart date formatting works as specified
- Smooth navigation between two screens
- Zero crashes during basic operations

### 8. Future Considerations (Not in V1.0)
- Expense categories
- Edit/delete functionality
- Data backup/restore
- Expense analytics
- Multiple currency support
- Receipt photo attachment

---

**Document Version:** 1.0  
**Last Updated:** [Current Date]  
**Status:** Draft - Pending Review
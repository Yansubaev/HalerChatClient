# HalerChat Android — концепция проекта и целевой стек

## 1. Цель проекта

Разработать полноценный Android-клиент для существующего backend-проекта **HalerChat**.

Проект должен быть не просто pet-project для портфолио, а учебной площадкой для отработки архитектуры современного Android-приложения уровня Middle:

- многомодульная архитектура;
- MVI / UDF;
- Koin;
- Ktor Client;
- полноценная работа с REST API;
- WebSocket;
- локальная база данных Room;
- offline-first и синхронизация;
- access/refresh token flow;
- фоновые задачи;
- тестирование;
- WebRTC для голосовых и видеозвонков.

## 2. Основная идея приложения

Android-приложение является клиентом существующей серверной системы HalerChat.

Backend уже реализует:

- регистрацию и вход;
- access/refresh token;
- комнаты;
- участников комнат;
- сообщения;
- историю сообщений;
- WebSocket-соединения;
- realtime-рассылку событий;
- signaling для voice/video;
- serverless-инфраструктуру на AWS.

Клиент должен использовать этот backend как реальный источник данных и реализовать полноценную Android-архитектуру вокруг него.

---

## 3. Связь с backend

Высокоуровневая схема:

```text
                     HalerChat Backend

               REST API        WebSocket
                   │               │
                   ▼               ▼

                 Ktor Client / Ktor WebSockets
                           │
                           ▼
                      Repository
                     ↙          ↘
                  Room        Sync logic
                   │
                   ▼
                  Flow
                   │
                   ▼
                MVI/UDF
                   │
                   ▼
                Compose
```

REST используется для:

- регистрации;
- входа;
- refresh token;
- списка комнат;
- создания комнаты;
- получения комнаты;
- изменения комнаты;
- удаления комнаты;
- join/leave;
- загрузки истории сообщений;
- операций, которые сервер предоставляет как HTTP API.

WebSocket используется для:

- подключения клиента;
- realtime-сообщений;
- событий комнат;
- отправки сообщений;
- получения новых сообщений;
- join/leave в realtime-режиме;
- WebRTC signaling;
- будущих realtime-событий.

---

## 4. Целевой стек

### Основной стек

```text
Kotlin
Jetpack Compose
Material 3

MVI / UDF
ViewModel
Coroutines
Flow
StateFlow
SharedFlow

Clean Architecture
Feature-based multi-module

Koin

Ktor Client
Ktor WebSockets
kotlinx.serialization

Room

WorkManager
DataStore

Navigation Compose или Navigation 3
```

### Тестирование

```text
JUnit
kotlinx-coroutines-test
Turbine
MockK или собственные Fake-реализации
Room tests
Compose UI tests
```

### Поздние этапы

```text
WebRTC
Foreground Service
Notifications
FCM
Camera / Microphone
Picture-in-Picture
Audio routing
Bluetooth headset
```

---

## 5. Room как локальный источник истины

В HalerChat Room должен стать локальным источником истины для UI, обеспечивая реактивное отображение сообщений и комнат, хранение локальных операций и синхронизацию с сервером.

Например:

```text
REST ───────┐
            │
WebSocket ──┼──> Repository ──> Room ──> Flow ──> UI
            │
Worker ─────┘
```

UI не должен зависеть напрямую от того, откуда пришли данные:

- REST;
- WebSocket;
- локальная очередь;
- background sync.

Все изменения сначала приводятся к локальной модели данных, после чего UI получает состояние реактивно через Flow.

---

## 6. MVI / UDF

Вместо MVVM проект должен использовать MVI/UDF-подход.

Базовая модель:

```text
User Intent
    │
    ▼
ViewModel
    │
    ▼
Reducer / state transformation
    │
    ▼
Immutable UiState
    │
    ▼
Compose
```

Одноразовые действия:

```text
UiEffect
├── Navigation
├── Snackbar
├── Permission request
└── External action
```

Пример состояний экрана чата:

```text
messages
connectionState
inputText
replyingTo
editingMessage
selectedMessages
loadingHistory
sendState
callState
```

---

## 7. Многомодульная архитектура

Начальная структура:

```text
:app

:core:model
:core:network
:core:database
:core:common
:core:designsystem

:feature:auth
:feature:rooms
:feature:chat
:feature:settings
```

Позже:

```text
:feature:profile
:feature:call

:core:notifications
:core:testing
```

Главная цель — не просто создать несколько Gradle-модулей, а научиться правильно проектировать dependency graph.

Необходимо избегать хаотических зависимостей между feature-модулями.

Предпочтительный принцип:

```text
feature modules
      │
      ▼
core/domain abstractions
      │
      ▼
data implementations
```

Границы архитектуры должны поддерживаться не только package-структурой, но и Gradle.

---

## 8. Auth

Backend уже предоставляет email/password auth и refresh token flow.

Клиент должен реализовать:

```text
Register
Login
Store session
Attach access token
Handle 401
Refresh access token
Retry original request
Logout
```

Особое внимание:

- конкурентным 401;
- единственному refresh-запросу;
- повтору ожидающих запросов;
- истёкшему refresh token;
- очистке локальной сессии.

---

## 9. Realtime и WebSocket

Клиент должен поддерживать:

- initial connect;
- authenticated connect;
- reconnect;
- backoff;
- connection state;
- resubscribe после reconnect;
- обработку входящих событий;
- отправку команд;
- восстановление состояния после разрыва соединения.

Состояние WebSocket не должно быть напрямую связано с конкретным Composable.

Соединение должно жить на уровне data/repository/service.

---

## 10. Offline-first

Одна из центральных учебных задач проекта.

Пример отправки сообщения:

```text
User presses Send
        │
        ▼
Create local message
status = PENDING
        │
        ▼
Save to Room
        │
        ▼
UI immediately displays it
        │
        ▼
Send to backend
        │
        ├── success → SENT
        │
        └── failure → FAILED
```

После восстановления сети:

```text
pending messages
      │
      ▼
sync worker
      │
      ▼
retry
      │
      ▼
server acknowledgement
      │
      ▼
update local state
```

Необходимо продумать:

- локальные ID;
- server ID;
- deduplication;
- retry;
- reconnect;
- порядок сообщений;
- временные метки;
- повторную доставку событий;
- конфликт REST и WebSocket ответов.

---

## 11. WebRTC

Звонки — поздний этап проекта.

Backend уже содержит signaling для voice/video.

После стабильной реализации текстового чата можно добавить:

- peer connection;
- SDP;
- ICE;
- STUN/TURN;
- microphone;
- camera;
- audio focus;
- audio routing;
- foreground service;
- call notifications;
- Picture-in-Picture;
- lifecycle handling.

WebRTC не должен блокировать разработку базового клиента.

---

## 12. Этапы разработки

### Этап 1 — Foundation

- multi-module;
- Koin;
- Ktor;
- Room;
- MVI;
- базовая navigation;
- дизайн-система.

### Этап 2 — Authentication

- register;
- login;
- session storage;
- access token;
- refresh token;
- logout.

### Этап 3 — REST

- rooms list;
- room details;
- create;
- update;
- delete;
- join;
- leave;
- history.

### Этап 4 — WebSocket

- connect;
- reconnect;
- realtime messages;
- room events;
- signaling transport.

### Этап 5 — Local source of truth

- Room;
- local entities;
- repositories;
- Flow;
- sync;
- retry;
- offline state.

### Этап 6 — Tests

- reducer tests;
- ViewModel tests;
- repository tests;
- Room tests;
- auth refresh tests;
- WebSocket event tests;
- sync tests.

### Этап 7 — Calls

- voice;
- video;
- foreground service;
- notifications;
- WebRTC lifecycle.

---

## 13. Итоговая учебная ценность

HalerChat Android объединяет следующие направления разработки:

```text
Clean Architecture
        ↓
Multi-module architecture
        ↓
MVI/UDF
        ↓
Koin
        ↓
Ktor REST
        ↓
WebSocket
        ↓
Room as source of truth
        ↓
Offline/realtime synchronization
        ↓
Authentication/session management
        ↓
Background sync
        ↓
WebRTC
        ↓
Testing complex state
```

Основная задача проекта — научиться строить не просто набор экранов, а полноценный Android-клиент для реальной распределённой backend-системы.

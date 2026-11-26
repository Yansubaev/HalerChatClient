Продолжаем учебный проект HalerChat (Kotlin Multiplatform: Android и iOS). Ты ведёшь меня по нему как ментор, правила в AGENTS.md.
Концепция и этапы: HalerChat_Project_Concept.md. Контракт бэкенда: HALERCHAT_CLIENT_SPEC.md.

## Что сделано

Этап 2 (Authentication) закрыт, этап 3 (REST) начат: список комнат. Проект переведён на KMP
(ветка feature/kmp-migration), собирается и работает на Android и на iPhone.

- Модули: core:model, core:network, core:data, core:designsystem, feature:auth, feature:rooms,
  composeApp, androidApp; iosApp — Swift-пакет вне Gradle.
- Весь код и UI в commonMain; платформенное через expect/actual:
  HTTP-движок (OkHttp / Darwin), путь к DataStore, тема (dynamic color только на Android).
- core:network: Ktor-клиент, AuthApi (login, register, logout, refresh), RoomsApi
  (getRooms, createRoom, room, deleteRoom, updateVisibility, resetInvite, leaveRoom,
  roomPreview, joinRoom), ApiException. Access token и refresh по 401 — плагин Ktor Auth (bearer),
  токены через интерфейс TokenStorage, его реализует SessionStore из core:data.
  Неудачный refresh (401) очищает сессию.
- core:data: SessionStore на DataStore; AuthRepository и RoomsRepository с Default-реализациями.
- feature:auth: логин и регистрация на MVI, строки на английском и русском (Compose Resources).
- feature:rooms: экран списка комнат (RoomsViewModel, Retry, Logout).
- composeApp: HalerChatApp (тема, Navigation 3 с navConfig для сериализации ключей), MainViewModel,
  Koin (initKoin), MainViewController для iOS.
- androidApp: Application с initKoin, MainActivity со сплэшем, пока читается сессия.
- iosApp: SwiftUI-обёртка над MainViewController. Сборка на Linux через omarchy-apple-dev:
  `iosApp/build.sh`, установка `xtool install iosApp/xtool/HalerChat.app`.
- Тесты в commonTest на kotlin.test: AuthApiTest, RoomsApiTest, HttpClientAuthTest, SessionStoreTest,
  DefaultAuthRepositoryTest, DefaultRoomsRepositoryTest, LoginViewModelTest, RegisterViewModelTest.
  Запуск: `./gradlew testAndroidHostTest`.

## Принятые решения (не предлагай менять без причины)

- TextField через value/onValueChange, а не TextFieldState: хочу чистый MVI.
- Навигация после логина — через состояние (isLoggedIn в State), а не через Channel с эффектами.
- В тестах рукописные фейки, без библиотек моков.
- Интерфейс выделяем только когда он нужен для фейка в тесте.
- Каждый Gradle-модуль объявляет свой Koin-модуль; классы по умолчанию internal.
- Зависимости в build.gradle.kts добавляю я сам, ты говоришь, какие нужны.
- Минимум кода: без абстракций «на будущее».
- Новый код пишем в commonMain; в androidMain/iosMain только то, у чего нет общего API.
- Только мультиплатформенные библиотеки (JetBrains lifecycle/navigation3, koin-compose-viewmodel,
  Room KMP); строки и картинки — Compose Resources, аргументы в строках позиционные (%1$d).
- Новый NavKey регистрируем в navSerializers своего модуля, иначе падает сохранение back stack.

## Что дальше

Этап 3: создание комнаты и присоединение к ней (спека: POST /rooms, GET и POST /rooms/join/{inviteToken}).
RoomsApi.createRoom, roomPreview и joinRoom уже есть; нужны репозиторий, экраны и навигация.
Открытые вопросы для разбора:
- как пользователь получает inviteToken: ввод вручную или deep link serverlesschat://join/{token}
  (на Android — intent-filter, на iOS — URL scheme в Info.plist);
- private-комнаты требуют accessCode при join;
- после создания или вступления обновить список комнат.

Дальше по концепции: остальной REST этапа 3 (детали, удаление, выход, история), этап 4 WebSocket, этап 5 Room.

## Отложено

- Шифрование refresh token (TODO в SessionStore): на Android — Tink + Keystore, на iOS — Keychain.
- Тест Koin verify().
- Таймаут регистрации с последующим 409 → попытка логина; обработка битого JSON.
- Текст VALIDATION_FAILED приходит с сервера по-английски и показывается как есть.
- Уборка: .idea/misc.xml в индексе Git, неиспользуемые ресурсы в androidApp (Lint: UnusedResources).
- iosApp/build.sh вручную докладывает Compose-ресурсы feature-модулей. Когда смержат
  joshuaswarren/omarchy-apple-dev#190 и я сделаю git pull в ~/omarchy-apple-dev, этот шаг убрать.
- Подпись бесплатным Apple ID живёт 7 дней: потом переустановить через xtool install.

Начни с разбора: какие экраны и состояния нужны для создания комнаты и вступления по приглашению, дай шаги, код пишу я.

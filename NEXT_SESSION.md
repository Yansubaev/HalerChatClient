Продолжаем учебный проект HalerChat Android. Ты ведёшь меня по нему как ментор, правила в AGENTS.md.
Концепция и этапы: HalerChat_Android_Project_Concept.md. Контракт бэкенда: HALERCHAT_CLIENT_SPEC.md.

## Что сделано

Этап 1 (Foundation) — частично, этап 2 (Authentication) — до logout включительно.

- Модули: core:model, core:network, core:data, core:designsystem, feature:auth, app.
- core:network: Ktor-клиент (createHttpClient), AuthApi (login, register, logout), ApiException.
- core:data: SessionStore на DataStore; AuthRepository (интерфейс) и DefaultAuthRepository
  с login, register, logout и isLoggedIn: Flow<Boolean>; AuthResult с sealed-уровнем Failure.
  logout очищает SessionStore всегда (и при 401, и без сети); без сессии на сервер не ходит.
- feature:auth: экраны логина и регистрации на MVI (Contract + ViewModel + Route/Screen),
  строки в ресурсах на английском и русском с префиксом auth_.
- app: Koin (appModule, dataModule, authModule), Navigation 3, MainViewModel выбирает
  стартовый экран по сохранённой сессии; HomeKey пока заглушка Text("Home") с кнопкой Logout.
  HalerChatApp сбрасывает back stack на LoginKey по isLoggedIn == false (LaunchedEffect) —
  этот же механизм сработает при неудачном refresh.
- MainActivity держит системный сплэш через OnPreDrawListener, пока сессия читается с диска.
- Тесты: AuthApiTest, SessionStoreTest, DefaultAuthRepositoryTest, LoginViewModelTest,
  RegisterViewModelTest; logout покрыт в AuthApiTest и DefaultAuthRepositoryTest (4 теста).

## Принятые решения (не предлагай менять без причины)

- TextField через value/onValueChange, а не TextFieldState: хочу чистый MVI.
- Навигация после логина — через состояние (isLoggedIn в State), а не через Channel с эффектами.
- В тестах рукописные фейки (FakeAuthRepository), без библиотек моков.
- Интерфейс выделяем только когда он нужен для фейка в тесте.
- Каждый Gradle-модуль объявляет свой Koin-модуль; классы по умолчанию internal.
- Зависимости в build.gradle.kts добавляю я сам, ты говоришь, какие нужны.
- Минимум кода: без абстракций «на будущее».

## Что дальше

Закрыть этап 2, по порядку:

1. ~~Logout~~ — готово (коммит c9177fd). Осталось проверить вручную на устройстве:
   после Logout экран логина, «Назад» не возвращает на Home.
2. Access token в запросах и refresh по 401 с повтором запроса (спека, раздел 3.3).
   Refresh token ротируется, повтор старого отзывает все сессии, поэтому refresh
   строго один в полёте. Открытые вопросы для разбора:
   - подходит ли плагин Ktor Auth (bearer) и сериализует ли он refresh сам — проверить по документации Ktor 3.6;
   - как core:network получит токены, если SessionStore лежит в core:data;
   - неудачный refresh → очистка сессии → экран логина (тот же механизм, что в п. 1).
3. Первый авторизованный запрос — список комнат (GET /rooms). Это начало этапа 3
   и способ проверить п. 2 на живом сервере.

Дальше по концепции: этап 3 REST (комнаты, история), этап 4 WebSocket, этап 5 Room.

## Отложено

- Шифрование refresh token (Tink + Keystore), TODO в SessionStore.
- Тест Koin verify().
- Таймаут регистрации с последующим 409 → попытка логина; обработка битого JSON.
- Текст VALIDATION_FAILED приходит с сервера по-английски и показывается как есть.
- Уборка: .idea/misc.xml в индексе Git, неиспользуемые ресурсы в app (Lint: UnusedResources).

Gradle-зависимости модулей вычищены до реально используемых: новые добавляем по мере
необходимости. Compose приходит в модули через api в core:designsystem.

Начни с п. 2: сверься с документацией Ktor по плагину Auth (bearer), объясни подход и дай шаги, код пишу я.

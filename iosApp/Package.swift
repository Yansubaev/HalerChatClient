// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "HalerChat",
    platforms: [.iOS("17.0")],
    products: [.library(name: "HalerChat", targets: ["HalerChat"])],
    targets: [
        // Собирается из composeApp: ~/omarchy-apple-dev/kmp/build-framework.sh --module :composeApp --dynamic --xcframework
        .binaryTarget(name: "ComposeApp", path: "Frameworks/ComposeApp.xcframework"),
        .target(name: "HalerChat", dependencies: ["ComposeApp"]),
    ]
)

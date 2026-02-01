import SwiftUI
import shared

@main
struct iOSApp: App {

    init() {
        // Initialize Koin/Shared Engine here if needed
        // Helper.initKoin()
    }

	var body: some Scene {
		WindowGroup {
			SplashView()
		}
	}
}

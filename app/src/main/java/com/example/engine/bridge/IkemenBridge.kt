package com.example.engine.bridge

/**
 * IkemenBridge acts as the JNI interface to the native Ikemen-GO engine.
 * In a production environment with the compiled .so files, these functions
 * would be declared as `external` and map directly to C/C++/Go bindings.
 */
object IkemenBridge {
    private var isEngineLoaded = false

    fun loadLibrary() {
        if (!isEngineLoaded) {
            try {
                // System.loadLibrary("ikemen")
                // isEngineLoaded = true
            } catch (e: UnsatisfiedLinkError) {
                e.printStackTrace()
            }
        }
    }

    fun initEngine(assetsPath: String, storagePath: String) {
        println("IkemenBridge: initEngine with assets=$assetsPath, storage=$storagePath")
    }

    fun startEngine() {
        println("IkemenBridge: startEngine")
    }
    
    fun stopEngine() {
        println("IkemenBridge: stopEngine")
    }

    fun pauseEngine() {
        println("IkemenBridge: pauseEngine")
    }

    fun resumeEngine() {
        println("IkemenBridge: resumeEngine")
    }

    fun sendTouchInput(action: Int, x: Float, y: Float, pointerId: Int) {
        println("IkemenBridge: sendTouchInput action=$action")
    }
    
    fun sendKeyEvent(keyCode: Int, isDown: Boolean) {
        println("IkemenBridge: sendKeyEvent code=$keyCode down=$isDown")
    }
}

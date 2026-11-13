# Default Android/AGP rules retain manifest entry points and framework callbacks.
# Current code uses no reflective app models, JNI or name-based serialization.
# Do not keep whole feature packages: allow R8 to optimize across module boundaries.
# Animator properties use typed View properties, so no name-based setter rules are needed.

# R8 Full Mode Aggressive Bytecode Optimization Rules
-repackageclasses ''
-allowaccessmodification
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

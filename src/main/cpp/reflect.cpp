#include "me_kcra_reflect_Reflect.h"
#include <iostream>

JNIEXPORT jobject JNICALL Java_me_kcra_reflect_Reflect_allocateInstance(JNIEnv* env, jobject thisObject, jclass klass) {
    return env->AllocObject(klass);
}
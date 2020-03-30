/**
 * Created by Elec332 on 10-2-2020
 */
#ifndef SDR_COMMON_HEADER
#define SDR_COMMON_HEADER

#include <stdbool.h>
#include <string.h>
#include <jni.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h> //Otherwise sqrt doesn't work properly

#define EXPORT JNIEXPORT

#endif
#ifndef HACKRF_LIB
#define HACKRF_LIB

#include <common_sdr_lib.h>
#include <hackrf.h>

typedef int (*javaCallback)(hackrf_transfer*);

#ifdef __cplusplus
extern "C" {
#endif

    EXPORT hackrf_device* hrfd_open(int deviceId);

    EXPORT bool hrfd_close(hackrf_device* device);

    EXPORT bool hrfd_setFrequency(hackrf_device* device, uint64_t frequency);

    EXPORT bool hrfd_setSampleRate(hackrf_device* device, int sampleRate);

    EXPORT bool hrfd_setLNAGain(hackrf_device* device, uint32_t gain);

    EXPORT bool hrfd_setVGAGain(hackrf_device* device, uint32_t gain);

    EXPORT bool hrfd_setBiasTeeEnabled(hackrf_device* device, bool enabled);

    EXPORT bool hrfd_setHWSyncMode(hackrf_device* device, uint8_t  gain);

    EXPORT bool hrfd_startRx(hackrf_device* device, javaCallback callBack);

    EXPORT bool hrfd_stopRx(hackrf_device* device);

    EXPORT int hrfd_getDeviceCountInternal();

    EXPORT void hrfd_getDevices(int *stringArray);

#ifdef __cplusplus
}
#endif
#endif
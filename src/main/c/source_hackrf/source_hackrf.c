/**
 * Created by Elec332 on 10-2-2020
 */
#include <source_hackrf.h>
#include <hackrf.h>

EXPORT hackrf_device* hrfd_open(int deviceId) {
	hackrf_device_list_t *list;

	int result = hackrf_init();
	if (result != HACKRF_SUCCESS) {
		return NULL;
	}

	list = hackrf_device_list();
	if (list->devicecount < deviceId) {
		return NULL;
	}

    hackrf_device* device;
    result = hackrf_device_list_open(list, deviceId, &device);
    if (result != HACKRF_SUCCESS) {
        device = NULL;
        return NULL;
    }
    return device;
}

EXPORT bool hrfd_close(hackrf_device* device) {
    if (device == NULL) {
        return false;
    }

    int result = hackrf_close(device);
    return result == HACKRF_SUCCESS;
}

EXPORT bool hrfd_setFrequency(hackrf_device* device, uint64_t frequency) {
    if (device == NULL) {
        return false;
    }

    int result = hackrf_set_freq(device, frequency);
    return result == HACKRF_SUCCESS;
}

EXPORT bool hrfd_setSampleRate(hackrf_device* device, int sampleRate){
    if (device == NULL) {
        return false;
    }

    int result = hackrf_set_sample_rate(device, sampleRate);
    if (result != HACKRF_SUCCESS) {
        return false;
    }
    uint32_t bWidth = hackrf_compute_baseband_filter_bw_round_down_lt(sampleRate);
    result = hackrf_set_baseband_filter_bandwidth(device, bWidth);
    return result == HACKRF_SUCCESS;
}

EXPORT bool hrfd_setLNAGain(hackrf_device* device, uint32_t gain){
    if (device == NULL) {
        return false;
    }

    int result = hackrf_set_lna_gain(device, gain);
    return result == HACKRF_SUCCESS;
}

EXPORT bool hrfd_setVGAGain(hackrf_device* device, uint32_t gain){
    if (device == NULL) {
        return false;
    }

    int result = hackrf_set_vga_gain(device, gain);
    return result == HACKRF_SUCCESS;
}

EXPORT bool hrfd_setBiasTeeEnabled(hackrf_device* device, bool enabled){
    if (device == NULL) {
        return false;
    }

    int result = hackrf_set_amp_enable(device, enabled);
    return result == HACKRF_SUCCESS;
}

EXPORT bool hrfd_setHWSyncMode(hackrf_device* device, uint8_t mode){
    if (device == NULL) {
        return false;
    }

    int result = hackrf_set_hw_sync_mode(device, mode);
    return result == HACKRF_SUCCESS;
}

EXPORT bool hrfd_startRx(hackrf_device* device, javaCallback callBack) {
    if (device == NULL) {
        return false;
    }

    int result = hackrf_start_rx(device, callBack, NULL);
    return result == HACKRF_SUCCESS;
}

EXPORT bool hrfd_stopRx(hackrf_device* device) {
    if (device == NULL) {
        return false;
    }

    int result = hackrf_stop_rx(device);
    return result == HACKRF_SUCCESS;
}

EXPORT int hrfd_getDeviceCountInternal() {
    hackrf_device_list_t *list;

	int result = hackrf_init();
	if (result != HACKRF_SUCCESS) {
		return -1;
	}

	list = hackrf_device_list();
	return list->devicecount;
}

EXPORT void hrfd_getDevices(int *stringArray) {
    hackrf_device_list_t *list;

	int result = hackrf_init();
	if (result != HACKRF_SUCCESS) {
		return;
	}

	list = hackrf_device_list();
    for (int i = 0; i < list->devicecount; i++) {
        char* data = list->serial_numbers[i];
        for (int c = 0; c < 32; c++) {
            stringArray[32 * i + c] = data[c];
        }
    }
}

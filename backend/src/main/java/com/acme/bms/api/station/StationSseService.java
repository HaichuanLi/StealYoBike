package com.acme.bms.api.station;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.acme.bms.domain.repo.StationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StationSseService {

    private final StationRepository stationRepository;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private volatile String lastPayloadJson = null;

    @Transactional(readOnly = true)
    public SseEmitter subscribe() {
        // default timeout: 30 minutes
        SseEmitter emitter = new SseEmitter(Duration.ofMinutes(30).toMillis());
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        // send an initial snapshot immediately
        try {
            StationListResponse payload = StationListResponse
                    .fromProjections(stationRepository.findAllStationSummaries());
            String json = null;
            try {
                json = mapper.writeValueAsString(payload);
                synchronized (this) {
                    lastPayloadJson = json;
                }
            } catch (JsonProcessingException e) {
            }

            if (json != null) {
                emitter.send(SseEmitter.event().name("stations-snapshot").data(json, MediaType.APPLICATION_JSON));
            } else {
                emitter.send(SseEmitter.event().name("stations-snapshot").data(payload, MediaType.APPLICATION_JSON));
            }
        } catch (IOException ex) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    @Transactional(readOnly = true)
    public void broadcastStations() {
        StationListResponse payload = StationListResponse.fromProjections(stationRepository.findAllStationSummaries());

        String payloadJson = null;
        try {
            payloadJson = mapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            payloadJson = null;
        }

        if (payloadJson != null) {
            synchronized (this) {
                if (payloadJson.equals(lastPayloadJson)) {
                    return;
                }
                lastPayloadJson = payloadJson;
            }
        }

        for (SseEmitter emitter : emitters) {
            try {
                String jsonToSend = payloadJson;
                if (jsonToSend == null) {
                    try {
                        jsonToSend = mapper.writeValueAsString(payload);
                    } catch (JsonProcessingException e) {
                        jsonToSend = null;
                    }
                }

                if (jsonToSend != null) {
                    emitter.send(
                            SseEmitter.event().name("stations-snapshot").data(jsonToSend, MediaType.APPLICATION_JSON));
                } else {
                    emitter.send(
                            SseEmitter.event().name("stations-snapshot").data(payload, MediaType.APPLICATION_JSON));
                }
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

}

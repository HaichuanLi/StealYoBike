package com.acme.bms.application.service;

public interface InitialStateResetter {
    record Result(int stationsRestored, int docksRestored, int bikesRestored) {}
    Result restore();
}

package br.com.gabryel.maplewood.model.dto;

import java.util.List;

public record RoomTypeData(
    List<Integer> specializationIds
) {}
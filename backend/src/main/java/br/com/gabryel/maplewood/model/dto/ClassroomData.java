package br.com.gabryel.maplewood.model.dto;

public record ClassroomData(
    int id,
    String name,
    int capacity,
    RoomTypeData roomType
) {}
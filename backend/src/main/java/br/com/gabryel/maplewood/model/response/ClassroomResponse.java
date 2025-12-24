package br.com.gabryel.maplewood.model.response;

public record ClassroomResponse(
    Integer id,
    String name,
    String roomTypeName,
    String equipment,
    Integer capacity
) {}

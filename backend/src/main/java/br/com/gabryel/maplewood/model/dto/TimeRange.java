package br.com.gabryel.maplewood.model.dto;

import br.com.gabryel.maplewood.model.Weekday;

public record TimeRange(Weekday weekday, int start, int end) { }
package org.notes.model.base;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResult<T> {
    private T data;
    private String token;
}

package org.notes.task.email;

import lombok.Data;

import java.io.Serializable;

@Data
public class WelcomeEmailTask implements Serializable {

    private String email;

    private String username;
}

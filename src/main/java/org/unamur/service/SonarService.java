package org.unamur.service;

import org.unamur.model.SonarIssue;

import java.util.List;
import java.util.Map;

public interface SonarService {

    Map<String, String> getSonarMetrics();

    List<SonarIssue> getIssues();

}

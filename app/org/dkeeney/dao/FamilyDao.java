package org.dkeeney.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dkeeney.models.FamilyMember;
import org.dkeeney.models.FamilyWrapper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FamilyDao {
  private ObjectMapper objectMapper = new ObjectMapper();

  public List<FamilyMember> loadFamily() {
    try {
      JsonNode inputJson = objectMapper.readTree(
          Files.readAllBytes(Paths.get(getClass().getResource("/family.json").toURI()))
      );
      FamilyWrapper familyWrapper = objectMapper.treeToValue(inputJson, FamilyWrapper.class);
      return familyWrapper.getFamily();
    } catch (IOException | URISyntaxException e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
  }
}

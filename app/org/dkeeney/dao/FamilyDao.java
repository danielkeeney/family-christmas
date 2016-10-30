package org.dkeeney.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dkeeney.models.FamilyMember;
import org.dkeeney.models.wrapper.FamilyWrapper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FamilyDao {
  public static final String FAMILY_JSON = "/family.json";
  private ObjectMapper objectMapper = new ObjectMapper();
  private List<FamilyMember> family = null;

  public List<FamilyMember> getFamily() {
    synchronized (this) {
      if (family == null) {
        family = loadFamilyFromFile();
      }
    }
    return this.family;
  }

  private List<FamilyMember> loadFamilyFromFile() {
    try {
      JsonNode inputJson = objectMapper.readTree(
          Files.readAllBytes(Paths.get(getClass().getResource(FAMILY_JSON).toURI()))
      );
      FamilyWrapper familyWrapper = objectMapper.treeToValue(inputJson, FamilyWrapper.class);
      return familyWrapper.getFamily();
    } catch (IOException | URISyntaxException e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
  }
}

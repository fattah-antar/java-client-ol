/*******************************************************************************
 * Copyright (c) 2017, 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
package io.openliberty.guides.consumingrest;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

import io.openliberty.guides.consumingrest.model.Album;
import io.openliberty.guides.consumingrest.model.Artist;

public class Consumer {
    public static Artist[] consumeWithJsonb(String targetUrl) {
      Client client = ClientBuilder.newClient();
      Response response = client.target(targetUrl).request().get();
      Artist[] artists = response.readEntity(Artist[].class);

      response.close();
      client.close();

      return artists;
    }

    public static Artist[] consumeWithJsonp(String targetUrl) {
      Client client = ClientBuilder.newClient();
      Response response = client.target(targetUrl).request().get();
      JsonArray arr = response.readEntity(JsonArray.class);

      response.close();
      client.close();

      return Consumer.collectArtists(arr);
    }

    private static Artist[] collectArtists(JsonArray artistArr) {
      List<Artist> artists = artistArr.stream().map(artistJson -> {
        JsonArray albumArr = ((JsonObject) artistJson).getJsonArray("albums");
        Artist artist = new Artist(
          ((JsonObject) artistJson).getString("name"),
          Consumer.collectAlbums(albumArr));
        return artist;
      }).collect(Collectors.toList());

      return artists.toArray(new Artist[artists.size()]);
    }

    private static Album[] collectAlbums(JsonArray albumArr) {
      List<Album> albums = albumArr.stream().map(albumJson -> {
        Album album = new Album(
          ((JsonObject) albumJson).getString("title"),
          ((JsonObject) albumJson).getString("artist"),
          ((JsonObject) albumJson).getInt("ntracks"));
        return album;
      }).collect(Collectors.toList());

      return albums.toArray(new Album[albums.size()]);
    }
}

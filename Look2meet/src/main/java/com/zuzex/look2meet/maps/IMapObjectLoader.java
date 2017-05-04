package com.zuzex.look2meet.maps;

import java.util.List;

/**
 * Created by dgureev on 6/27/14.
 */
public interface IMapObjectLoader {
    public void objectLoaded(MapObjectModel mapObject);
    public void objectsLoaded(List<MapObjectModel> mapObject);
}



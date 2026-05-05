package com.bloommood.web;

import com.bloommood.controller.PlantController;
import com.bloommood.dto.PlantUpdateRequest;
import com.bloommood.entity.Plant;
import com.bloommood.service.PlantService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlantControllerIntegrationTests {

    private static Authentication authWithUid(String uid) {
        Authentication a = mock(Authentication.class);
        when(a.getName()).thenReturn(uid);
        return a;
    }

    @Test
    void today_unauthorized_whenNoAuth() {
        PlantService plantService = mock(PlantService.class);
        PlantController c = new PlantController(plantService);

        var resp = c.getToday(null);
        assertEquals(401, resp.getStatusCode().value());
    }

    @Test
    void month_invalidYm_400() {
        PlantService plantService = mock(PlantService.class);
        PlantController c = new PlantController(plantService);

        var resp = c.getMonth("bad", authWithUid("1"));
        assertEquals(400, resp.getStatusCode().value());
        verifyNoInteractions(plantService);
    }

    @Test
    void updatePlant_notFound_404() {
        PlantService plantService = mock(PlantService.class);
        when(plantService.updatePlant(eq(1L), eq(10L), any(PlantUpdateRequest.class)))
                .thenThrow(new IllegalArgumentException("plant not found"));

        PlantController c = new PlantController(plantService);
        var resp = c.updatePlant(10L, new PlantUpdateRequest(), authWithUid("1"));
        assertEquals(404, resp.getStatusCode().value());
    }

    @Test
    void patch_and_delete_ok() {
        PlantService plantService = mock(PlantService.class);
        when(plantService.updatePlant(eq(1L), eq(10L), any(PlantUpdateRequest.class)))
                .thenReturn(new Plant());
        doNothing().when(plantService).deletePlant(1L, 10L);

        PlantController c = new PlantController(plantService);

        var updateResp = c.updatePlant(10L, new PlantUpdateRequest(), authWithUid("1"));
        assertEquals(200, updateResp.getStatusCode().value());

        var deleteResp = c.deletePlant(10L, authWithUid("1"));
        assertEquals(200, deleteResp.getStatusCode().value());
        verify(plantService).deletePlant(1L, 10L);
    }
}
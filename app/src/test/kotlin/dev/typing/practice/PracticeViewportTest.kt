package dev.typing.practice

import org.junit.Assert.assertEquals
import org.junit.Test

class PracticeViewportTest {
    @Test fun hideOnlyMovesByTheAllowedTravel() {
        val p = PracticeViewport(48, 320)
        p.update(400, 900, 350, 24, true)
        assertEquals(350, p.bottom(350, 24, 24))
        p.update(400, 900, 0, 24, false)
        assertEquals(302, p.bottom(0, 24, 24))
        assertEquals(330, p.bottom(330, 24, 24))
        assertEquals(302, p.bottom(100, 24, 24))
    }

    @Test fun geometryAndFloatingKeyboardInvalidateOldReserve() {
        val p = PracticeViewport(48, 320)
        p.update(400, 900, 350, 24, true)
        p.update(700, 500, 0, 24, false)
        assertEquals(24, p.bottom(0, 24, 24))
        p.update(700, 500, 200, 24, true)
        p.update(700, 500, 0, 24, true)
        assertEquals(24, p.bottom(0, 24, 24))
    }

    @Test fun smallWindowKeepsUsableSpaceAndNeverCoversIme() {
        val p = PracticeViewport(48, 320)
        p.update(400, 500, 300, 24, true)
        assertEquals(300, p.bottom(300, 24, 24))
        p.update(400, 500, 0, 24, false)
        assertEquals(156, p.bottom(0, 24, 24))
    }

    @Test fun restorationRetainsAnchorOnlyForSameGeometry() {
        val p = PracticeViewport(48, 320)
        p.restore(350, 400, 900)
        p.update(0, 0, 0, 24, false)
        p.update(400, 900, 0, 24, false)
        assertEquals(302, p.bottom(0, 24, 24))
        p.update(400, 700, 0, 24, false)
        assertEquals(24, p.bottom(0, 24, 24))
    }
}

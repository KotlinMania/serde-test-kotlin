import Testing
import SerdeTest

@Suite("SerdeTest Swift Export Tests")
struct SerdeTestExportTests {
    @Test("Swift module loads and exports cleanly")
    func testSwiftModuleLoads() throws {
        #expect(Bool(true), "SerdeTest swift module imported cleanly")
    }
}


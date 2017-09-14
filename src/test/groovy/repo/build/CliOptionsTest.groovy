package repo.build;

/**
 * Markelov Ruslan markelov@jet.msk.su
 */
public class CliOptionsTest extends GroovyTestCase {
    void testAllMvmSwitches() {
        def cli = CliBuilderFactory.build(null)
        def options = new CliOptions(cli.parse(['-me',
                                                '-mfae',
                                                '-mgs', 'mgs',
                                                '-mlr', 'mlr',
                                                '-mo',
                                                '-mP', 'mP1,mP2',
                                                '-ms', 'ms',
                                                '-mT', 'mT',
                                                '-mU']))

        assertTrue(options.hasMe())
        assertTrue(options.hasMfae())
        assertEquals(new File('mgs'), options.getMgs())
        assertEquals(new File('mlr'), options.getMlr())
        assertTrue(options.hasMo())
        assertArrayEquals(['mP1', 'mP2'].toArray(), options.getMP().toArray())
        assertEquals(new File('ms'), options.getMs())
        assertEquals('mT', options.getMT())
        assertTrue(options.hasMU())
    }

    void testNoneMvmSwitches() {
        def cli = CliBuilderFactory.build(null)
        def options = new CliOptions(cli.parse([]))

        assertFalse(options.hasMe())
        assertFalse(options.hasMfae())
        assertNull(options.getMgs())
        assertNull(options.getMlr())
        assertFalse(options.hasMo())
        assertNull(options.getMP())
        assertNull(options.getMs())
        assertNull(options.getMT())
        assertFalse(options.hasMU())
    }

}

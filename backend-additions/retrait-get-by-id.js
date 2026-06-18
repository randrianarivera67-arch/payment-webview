/* =========================================================================
 *  ROUTE VAOVAO hoampiana ao amin'ny  routes/retrait.js
 *  (ny site payment WebView mila izy mba haka ordre tokana)
 *
 *  APETRAKA: alefaso ALOHAN'ny  "module.exports = router;"  ao amin'ny
 *            routes/retrait.js ity block manaraka ity.
 * ========================================================================= */

// GET /api/retrait/:id — maka ordre (retrait na depot) tokana
router.get('/:id', auth, async (req, res) => {
  try {
    const r = await Retrait.findById(req.params.id);
    if (!r) return res.status(404).json({ error: 'Commande non trouvée' });
    res.json(r);
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

/* -------------------------------------------------------------------------
 *  SAFIDY: raha tianao ho azon'ny CLIENT (tsy admin) jerena ny ordre-ny
 *  ao amin'ny WebView nefa tsy manana JWT admin izy, esory ny `auth` eo
 *  ambony ka soloy version "public-by-id" toa ity (ny _id ObjectId no token):
 *
 *  router.get('/public/:id', async (req, res) => {
 *    try {
 *      const r = await Retrait.findById(req.params.id)
 *        .select('type operator numero montant ussdCode channel status createdAt');
 *      if (!r) return res.status(404).json({ error: 'Commande non trouvée' });
 *      res.json(r);
 *    } catch (e) { res.status(500).json({ error: e.message }); }
 *  });
 *
 *  → dia ovay ao amin'ny config.js:  getOrder: (id) => `/api/retrait/public/${id}`
 * ------------------------------------------------------------------------- */

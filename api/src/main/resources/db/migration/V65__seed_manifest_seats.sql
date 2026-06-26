-- =============================================================================
-- V65 — Seed Manifest Seats
-- =============================================================================

-- ================== SEATS FOR MANIFEST M200001 ==================
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200001-M100001-SEC-ORCH-L-row-A', 'M200001-M100001-SEC-ORCH-L', 'A')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200001-M100001-SEC-ORCH-L-row-B', 'M200001-M100001-SEC-ORCH-L', 'B')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200001-M100001-SEC-ORCH-L-row-C', 'M200001-M100001-SEC-ORCH-L', 'C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200001-M100001-SEC-ORCH-L-row-D', 'M200001-M100001-SEC-ORCH-L', 'D')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200001-M100001-SEC-ORCH-L-row-E', 'M200001-M100001-SEC-ORCH-L', 'E')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200001-M100001-SEC-ORCH-L-row-F', 'M200001-M100001-SEC-ORCH-L', 'F')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200001-M100001-SEC-ORCH-C-row-A', 'M200001-M100001-SEC-ORCH-C', 'A')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200001-M100001-SEC-ORCH-C-row-B', 'M200001-M100001-SEC-ORCH-C', 'B')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200001-M100001-SEC-ORCH-C-row-C', 'M200001-M100001-SEC-ORCH-C', 'C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200001-M100001-SEC-ORCH-C-row-D', 'M200001-M100001-SEC-ORCH-C', 'D')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200001-M100001-SEC-ORCH-C-row-E', 'M200001-M100001-SEC-ORCH-C', 'E')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200001-M100001-SEC-ORCH-C-row-F', 'M200001-M100001-SEC-ORCH-C', 'F')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200001-M100001-SEC-ORCH-R-row-A', 'M200001-M100001-SEC-ORCH-R', 'A')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200001-M100001-SEC-ORCH-R-row-B', 'M200001-M100001-SEC-ORCH-R', 'B')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200001-M100001-SEC-ORCH-R-row-C', 'M200001-M100001-SEC-ORCH-R', 'C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200001-M100001-SEC-ORCH-R-row-D', 'M200001-M100001-SEC-ORCH-R', 'D')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200001-M100001-SEC-ORCH-R-row-E', 'M200001-M100001-SEC-ORCH-R', 'E')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200001-M100001-SEC-ORCH-R-row-F', 'M200001-M100001-SEC-ORCH-R', 'F')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-A-s001', 'M200001-M100001-SEC-ORCH-L-row-A', '001', 212.63847995832748, 237.43895612986114, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-A-s002', 'M200001-M100001-SEC-ORCH-L-row-A', '002', 230.85499920849622, 248.29818103448932, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-A-s003', 'M200001-M100001-SEC-ORCH-L-row-A', '003', 247.47529429167844, 261.47126459412, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-A-s004', 'M200001-M100001-SEC-ORCH-L-row-A', '004', 262.2073645815428, 276.72676996416794, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-A-s005', 'M200001-M100001-SEC-ORCH-L-row-A', '005', 274.7923835211436, 293.7966743989019, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-A-s006', 'M200001-M100001-SEC-ORCH-L-row-A', '006', 285.009245925864, 312.3810781214881, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-B-s001', 'M200001-M100001-SEC-ORCH-L-row-B', '001', 220.81030895363932, 221.40083869447054, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-B-s002', 'M200001-M100001-SEC-ORCH-L-row-B', '002', 241.07618661945202, 233.48172640086935, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-B-s003', 'M200001-M100001-SEC-ORCH-L-row-B', '003', 259.56626489949224, 248.1367818609585, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-B-s004', 'M200001-M100001-SEC-ORCH-L-row-B', '004', 275.95569309696634, 265.1085315851368, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-B-s005', 'M200001-M100001-SEC-ORCH-L-row-B', '005', 289.95652666727227, 284.0988002687784, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-B-s006', 'M200001-M100001-SEC-ORCH-L-row-B', '006', 301.3227860925237, 304.7739494101555, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-C-s001', 'M200001-M100001-SEC-ORCH-L-row-C', '001', 228.9821379489512, 205.3627212590799, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-C-s002', 'M200001-M100001-SEC-ORCH-L-row-C', '002', 247.703759701878, 216.24438896306447, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-C-s003', 'M200001-M100001-SEC-ORCH-L-row-C', '003', 265.11073782116836, 229.1248752052119, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-C-s004', 'M200001-M100001-SEC-ORCH-L-row-C', '004', 280.99060086637564, 243.84695939003655, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-C-s005', 'M200001-M100001-SEC-ORCH-L-row-C', '005', 295.14951752730985, 260.23094218019844, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-C-s006', 'M200001-M100001-SEC-ORCH-L-row-C', '006', 307.41466254978303, 278.07683892587374, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-D-s001', 'M200001-M100001-SEC-ORCH-L-row-D', '001', 237.15396694426303, 189.3246038236893, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-D-s002', 'M200001-M100001-SEC-ORCH-L-row-D', '002', 257.5949213071525, 201.2056083576316, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-D-s003', 'M200001-M100001-SEC-ORCH-L-row-D', '003', 276.6004994578062, 215.26899639752727, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-D-s004', 'M200001-M100001-SEC-ORCH-L-row-D', '004', 293.93871727247137, 231.3431087217746, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-D-s005', 'M200001-M100001-SEC-ORCH-L-row-D', '005', 309.3979426063485, 249.23174299266563, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-D-s006', 'M200001-M100001-SEC-ORCH-L-row-D', '006', 322.7894784982325, 268.7165486231479, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-E-s001', 'M200001-M100001-SEC-ORCH-L-row-E', '001', 245.32579593957487, 173.28648638829867, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-E-s002', 'M200001-M100001-SEC-ORCH-L-row-E', '002', 267.48608291242704, 186.16682775219877, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-E-s003', 'M200001-M100001-SEC-ORCH-L-row-E', '003', 288.09026109444414, 201.41311758984267, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-E-s004', 'M200001-M100001-SEC-ORCH-L-row-E', '004', 306.8868336785671, 218.83925805351265, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-E-s005', 'M200001-M100001-SEC-ORCH-L-row-E', '005', 323.6463676853872, 238.23254380513282, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-E-s006', 'M200001-M100001-SEC-ORCH-L-row-E', '006', 338.16429444668194, 259.356258320422, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-E-s007', 'M200001-M100001-SEC-ORCH-L-row-E', '007', 350.26340659250275, 281.9525632761577, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-F-s001', 'M200001-M100001-SEC-ORCH-L-row-F', '001', 253.49762493488672, 157.24836895290807, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-F-s002', 'M200001-M100001-SEC-ORCH-L-row-F', '002', 277.37724451770157, 171.1280471467659, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-F-s003', 'M200001-M100001-SEC-ORCH-L-row-F', '003', 299.58002273108207, 187.55723878215804, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-F-s004', 'M200001-M100001-SEC-ORCH-L-row-F', '004', 319.8349500846628, 206.33540738525068, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-F-s005', 'M200001-M100001-SEC-ORCH-L-row-F', '005', 337.8947927644258, 227.23334461760004, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-F-s006', 'M200001-M100001-SEC-ORCH-L-row-F', '006', 353.5391103951314, 249.99596801769613, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-L-row-F-s007', 'M200001-M100001-SEC-ORCH-L-row-F', '007', 366.5769467591625, 274.34543456482515, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-A-s001', 'M200001-M100001-SEC-ORCH-C-row-A', '001', 290.82, 326.59, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-A-s002', 'M200001-M100001-SEC-ORCH-C-row-A', '002', 295.9, 344.01, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-A-s003', 'M200001-M100001-SEC-ORCH-C-row-A', '003', 298.97, 361.89, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-A-s004', 'M200001-M100001-SEC-ORCH-C-row-A', '004', 300, 380, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-A-s005', 'M200001-M100001-SEC-ORCH-C-row-A', '005', 298.97, 398.11, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-A-s006', 'M200001-M100001-SEC-ORCH-C-row-A', '006', 295.9, 415.99, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-A-s007', 'M200001-M100001-SEC-ORCH-C-row-A', '007', 290.82, 433.41, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-B-s001', 'M200001-M100001-SEC-ORCH-C-row-B', '001', 307.79, 320.58, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-B-s002', 'M200001-M100001-SEC-ORCH-C-row-B', '002', 313.44, 339.96, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-B-s003', 'M200001-M100001-SEC-ORCH-C-row-B', '003', 316.86, 359.85, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-B-s004', 'M200001-M100001-SEC-ORCH-C-row-B', '004', 318, 380, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-B-s005', 'M200001-M100001-SEC-ORCH-C-row-B', '005', 316.86, 400.15, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-B-s006', 'M200001-M100001-SEC-ORCH-C-row-B', '006', 313.44, 420.04, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-B-s007', 'M200001-M100001-SEC-ORCH-C-row-B', '007', 307.79, 439.42, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-C-s001', 'M200001-M100001-SEC-ORCH-C-row-C', '001', 324.76, 314.57, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-C-s002', 'M200001-M100001-SEC-ORCH-C-row-C', '002', 330.98, 335.91, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-C-s003', 'M200001-M100001-SEC-ORCH-C-row-C', '003', 334.74, 357.81, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-C-s004', 'M200001-M100001-SEC-ORCH-C-row-C', '004', 336, 380, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-C-s005', 'M200001-M100001-SEC-ORCH-C-row-C', '005', 334.74, 402.19, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-C-s006', 'M200001-M100001-SEC-ORCH-C-row-C', '006', 330.98, 424.09, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-C-s007', 'M200001-M100001-SEC-ORCH-C-row-C', '007', 324.76, 445.43, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-D-s001', 'M200001-M100001-SEC-ORCH-C-row-D', '001', 341.73, 308.57, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-D-s002', 'M200001-M100001-SEC-ORCH-C-row-D', '002', 348.52, 331.86, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-D-s003', 'M200001-M100001-SEC-ORCH-C-row-D', '003', 352.62, 355.77, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-D-s004', 'M200001-M100001-SEC-ORCH-C-row-D', '004', 354, 380, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-D-s005', 'M200001-M100001-SEC-ORCH-C-row-D', '005', 352.62, 404.23, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-D-s006', 'M200001-M100001-SEC-ORCH-C-row-D', '006', 348.52, 428.14, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-D-s007', 'M200001-M100001-SEC-ORCH-C-row-D', '007', 341.73, 451.43, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-E-s001', 'M200001-M100001-SEC-ORCH-C-row-E', '001', 358.69, 302.56, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-E-s002', 'M200001-M100001-SEC-ORCH-C-row-E', '002', 366.05, 327.81, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-E-s003', 'M200001-M100001-SEC-ORCH-C-row-E', '003', 370.51, 353.74, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-E-s004', 'M200001-M100001-SEC-ORCH-C-row-E', '004', 372, 380, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-E-s005', 'M200001-M100001-SEC-ORCH-C-row-E', '005', 370.51, 406.26, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-E-s006', 'M200001-M100001-SEC-ORCH-C-row-E', '006', 366.05, 432.19, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-E-s007', 'M200001-M100001-SEC-ORCH-C-row-E', '007', 358.69, 457.44, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-F-s001', 'M200001-M100001-SEC-ORCH-C-row-F', '001', 375.66, 296.55, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-F-s002', 'M200001-M100001-SEC-ORCH-C-row-F', '002', 383.59, 323.76, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-F-s003', 'M200001-M100001-SEC-ORCH-C-row-F', '003', 388.39, 351.7, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-F-s004', 'M200001-M100001-SEC-ORCH-C-row-F', '004', 390, 380, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-F-s005', 'M200001-M100001-SEC-ORCH-C-row-F', '005', 388.39, 408.3, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-F-s006', 'M200001-M100001-SEC-ORCH-C-row-F', '006', 383.59, 436.24, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-C-row-F-s007', 'M200001-M100001-SEC-ORCH-C-row-F', '007', 375.66, 463.45, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-A-s001', 'M200001-M100001-SEC-ORCH-R-row-A', '001', 285.009245925864, 447.6189218785119, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-A-s002', 'M200001-M100001-SEC-ORCH-R-row-A', '002', 274.7923835211436, 466.2033256010981, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-A-s003', 'M200001-M100001-SEC-ORCH-R-row-A', '003', 262.2073645815428, 483.27323003583206, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-A-s004', 'M200001-M100001-SEC-ORCH-R-row-A', '004', 247.47529429167844, 498.52873540588, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-A-s005', 'M200001-M100001-SEC-ORCH-R-row-A', '005', 230.85499920849622, 511.7018189655107, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-A-s006', 'M200001-M100001-SEC-ORCH-R-row-A', '006', 212.63847995832748, 522.5610438701389, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-B-s001', 'M200001-M100001-SEC-ORCH-R-row-B', '001', 301.3227860925237, 455.2260505898445, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-B-s002', 'M200001-M100001-SEC-ORCH-R-row-B', '002', 289.95652666727227, 475.9011997312216, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-B-s003', 'M200001-M100001-SEC-ORCH-R-row-B', '003', 275.95569309696634, 494.8914684148632, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-B-s004', 'M200001-M100001-SEC-ORCH-R-row-B', '004', 259.56626489949224, 511.86321813904146, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-B-s005', 'M200001-M100001-SEC-ORCH-R-row-B', '005', 241.07618661945202, 526.5182735991307, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-B-s006', 'M200001-M100001-SEC-ORCH-R-row-B', '006', 220.81030895363932, 538.5991613055295, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-C-s001', 'M200001-M100001-SEC-ORCH-R-row-C', '001', 317.6363262591834, 462.8331793011771, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-C-s002', 'M200001-M100001-SEC-ORCH-R-row-C', '002', 307.41466254978303, 481.92316107412626, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-C-s003', 'M200001-M100001-SEC-ORCH-R-row-C', '003', 295.14951752730985, 499.76905781980156, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-C-s004', 'M200001-M100001-SEC-ORCH-R-row-C', '004', 280.99060086637564, 516.1530406099635, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-C-s005', 'M200001-M100001-SEC-ORCH-R-row-C', '005', 265.11073782116836, 530.875124794788, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-C-s006', 'M200001-M100001-SEC-ORCH-R-row-C', '006', 247.70375970187797, 543.7556110369355, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-D-s001', 'M200001-M100001-SEC-ORCH-R-row-D', '001', 333.9498664258431, 470.44030801250966, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-D-s002', 'M200001-M100001-SEC-ORCH-R-row-D', '002', 322.7894784982325, 491.2834513768521, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-D-s003', 'M200001-M100001-SEC-ORCH-R-row-D', '003', 309.3979426063486, 510.76825700733434, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-D-s004', 'M200001-M100001-SEC-ORCH-R-row-D', '004', 293.93871727247137, 528.6568912782254, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-D-s005', 'M200001-M100001-SEC-ORCH-R-row-D', '005', 276.6004994578062, 544.7310036024727, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-D-s006', 'M200001-M100001-SEC-ORCH-R-row-D', '006', 257.5949213071525, 558.7943916423684, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-E-s001', 'M200001-M100001-SEC-ORCH-R-row-E', '001', 350.26340659250275, 478.0474367238423, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-E-s002', 'M200001-M100001-SEC-ORCH-R-row-E', '002', 338.16429444668194, 500.643741679578, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-E-s003', 'M200001-M100001-SEC-ORCH-R-row-E', '003', 323.6463676853872, 521.7674561948671, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-E-s004', 'M200001-M100001-SEC-ORCH-R-row-E', '004', 306.8868336785671, 541.1607419464874, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-E-s005', 'M200001-M100001-SEC-ORCH-R-row-E', '005', 288.09026109444414, 558.5868824101573, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-E-s006', 'M200001-M100001-SEC-ORCH-R-row-E', '006', 267.486082912427, 573.8331722478013, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-E-s007', 'M200001-M100001-SEC-ORCH-R-row-E', '007', 245.32579593957487, 586.7135136117013, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-F-s001', 'M200001-M100001-SEC-ORCH-R-row-F', '001', 366.5769467591625, 485.65456543517485, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-F-s002', 'M200001-M100001-SEC-ORCH-R-row-F', '002', 353.5391103951314, 510.0040319823039, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-F-s003', 'M200001-M100001-SEC-ORCH-R-row-F', '003', 337.89479276442586, 532.7666553823999, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-F-s004', 'M200001-M100001-SEC-ORCH-R-row-F', '004', 319.8349500846628, 553.6645926147494, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-F-s005', 'M200001-M100001-SEC-ORCH-R-row-F', '005', 299.58002273108207, 572.442761217842, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-F-s006', 'M200001-M100001-SEC-ORCH-R-row-F', '006', 277.3772445177015, 588.8719528532341, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200001-M100001-SEC-ORCH-R-row-F-s007', 'M200001-M100001-SEC-ORCH-R-row-F', '007', 253.49762493488672, 602.7516310470919, 'AVAILABLE', NULL, 'M200001-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;

-- ================== SEATS FOR MANIFEST M200002 ==================
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200002-M100001-SEC-ORCH-L-row-A', 'M200002-M100001-SEC-ORCH-L', 'A')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200002-M100001-SEC-ORCH-L-row-B', 'M200002-M100001-SEC-ORCH-L', 'B')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200002-M100001-SEC-ORCH-L-row-C', 'M200002-M100001-SEC-ORCH-L', 'C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200002-M100001-SEC-ORCH-L-row-D', 'M200002-M100001-SEC-ORCH-L', 'D')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200002-M100001-SEC-ORCH-L-row-E', 'M200002-M100001-SEC-ORCH-L', 'E')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200002-M100001-SEC-ORCH-L-row-F', 'M200002-M100001-SEC-ORCH-L', 'F')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200002-M100001-SEC-ORCH-C-row-A', 'M200002-M100001-SEC-ORCH-C', 'A')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200002-M100001-SEC-ORCH-C-row-B', 'M200002-M100001-SEC-ORCH-C', 'B')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200002-M100001-SEC-ORCH-C-row-C', 'M200002-M100001-SEC-ORCH-C', 'C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200002-M100001-SEC-ORCH-C-row-D', 'M200002-M100001-SEC-ORCH-C', 'D')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200002-M100001-SEC-ORCH-C-row-E', 'M200002-M100001-SEC-ORCH-C', 'E')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200002-M100001-SEC-ORCH-C-row-F', 'M200002-M100001-SEC-ORCH-C', 'F')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200002-M100001-SEC-ORCH-R-row-A', 'M200002-M100001-SEC-ORCH-R', 'A')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200002-M100001-SEC-ORCH-R-row-B', 'M200002-M100001-SEC-ORCH-R', 'B')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200002-M100001-SEC-ORCH-R-row-C', 'M200002-M100001-SEC-ORCH-R', 'C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200002-M100001-SEC-ORCH-R-row-D', 'M200002-M100001-SEC-ORCH-R', 'D')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200002-M100001-SEC-ORCH-R-row-E', 'M200002-M100001-SEC-ORCH-R', 'E')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200002-M100001-SEC-ORCH-R-row-F', 'M200002-M100001-SEC-ORCH-R', 'F')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-A-s001', 'M200002-M100001-SEC-ORCH-L-row-A', '001', 212.63847995832748, 237.43895612986114, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-A-s002', 'M200002-M100001-SEC-ORCH-L-row-A', '002', 230.85499920849622, 248.29818103448932, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-A-s003', 'M200002-M100001-SEC-ORCH-L-row-A', '003', 247.47529429167844, 261.47126459412, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-A-s004', 'M200002-M100001-SEC-ORCH-L-row-A', '004', 262.2073645815428, 276.72676996416794, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-A-s005', 'M200002-M100001-SEC-ORCH-L-row-A', '005', 274.7923835211436, 293.7966743989019, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-A-s006', 'M200002-M100001-SEC-ORCH-L-row-A', '006', 285.009245925864, 312.3810781214881, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-B-s001', 'M200002-M100001-SEC-ORCH-L-row-B', '001', 220.81030895363932, 221.40083869447054, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-B-s002', 'M200002-M100001-SEC-ORCH-L-row-B', '002', 241.07618661945202, 233.48172640086935, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-B-s003', 'M200002-M100001-SEC-ORCH-L-row-B', '003', 259.56626489949224, 248.1367818609585, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-B-s004', 'M200002-M100001-SEC-ORCH-L-row-B', '004', 275.95569309696634, 265.1085315851368, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-B-s005', 'M200002-M100001-SEC-ORCH-L-row-B', '005', 289.95652666727227, 284.0988002687784, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-B-s006', 'M200002-M100001-SEC-ORCH-L-row-B', '006', 301.3227860925237, 304.7739494101555, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-C-s001', 'M200002-M100001-SEC-ORCH-L-row-C', '001', 228.9821379489512, 205.3627212590799, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-C-s002', 'M200002-M100001-SEC-ORCH-L-row-C', '002', 247.703759701878, 216.24438896306447, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-C-s003', 'M200002-M100001-SEC-ORCH-L-row-C', '003', 265.11073782116836, 229.1248752052119, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-C-s004', 'M200002-M100001-SEC-ORCH-L-row-C', '004', 280.99060086637564, 243.84695939003655, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-C-s005', 'M200002-M100001-SEC-ORCH-L-row-C', '005', 295.14951752730985, 260.23094218019844, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-C-s006', 'M200002-M100001-SEC-ORCH-L-row-C', '006', 307.41466254978303, 278.07683892587374, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-D-s001', 'M200002-M100001-SEC-ORCH-L-row-D', '001', 237.15396694426303, 189.3246038236893, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-D-s002', 'M200002-M100001-SEC-ORCH-L-row-D', '002', 257.5949213071525, 201.2056083576316, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-D-s003', 'M200002-M100001-SEC-ORCH-L-row-D', '003', 276.6004994578062, 215.26899639752727, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-D-s004', 'M200002-M100001-SEC-ORCH-L-row-D', '004', 293.93871727247137, 231.3431087217746, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-D-s005', 'M200002-M100001-SEC-ORCH-L-row-D', '005', 309.3979426063485, 249.23174299266563, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-D-s006', 'M200002-M100001-SEC-ORCH-L-row-D', '006', 322.7894784982325, 268.7165486231479, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-E-s001', 'M200002-M100001-SEC-ORCH-L-row-E', '001', 245.32579593957487, 173.28648638829867, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-E-s002', 'M200002-M100001-SEC-ORCH-L-row-E', '002', 267.48608291242704, 186.16682775219877, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-E-s003', 'M200002-M100001-SEC-ORCH-L-row-E', '003', 288.09026109444414, 201.41311758984267, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-E-s004', 'M200002-M100001-SEC-ORCH-L-row-E', '004', 306.8868336785671, 218.83925805351265, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-E-s005', 'M200002-M100001-SEC-ORCH-L-row-E', '005', 323.6463676853872, 238.23254380513282, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-E-s006', 'M200002-M100001-SEC-ORCH-L-row-E', '006', 338.16429444668194, 259.356258320422, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-E-s007', 'M200002-M100001-SEC-ORCH-L-row-E', '007', 350.26340659250275, 281.9525632761577, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-F-s001', 'M200002-M100001-SEC-ORCH-L-row-F', '001', 253.49762493488672, 157.24836895290807, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-F-s002', 'M200002-M100001-SEC-ORCH-L-row-F', '002', 277.37724451770157, 171.1280471467659, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-F-s003', 'M200002-M100001-SEC-ORCH-L-row-F', '003', 299.58002273108207, 187.55723878215804, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-F-s004', 'M200002-M100001-SEC-ORCH-L-row-F', '004', 319.8349500846628, 206.33540738525068, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-F-s005', 'M200002-M100001-SEC-ORCH-L-row-F', '005', 337.8947927644258, 227.23334461760004, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-F-s006', 'M200002-M100001-SEC-ORCH-L-row-F', '006', 353.5391103951314, 249.99596801769613, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-L-row-F-s007', 'M200002-M100001-SEC-ORCH-L-row-F', '007', 366.5769467591625, 274.34543456482515, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-A-s001', 'M200002-M100001-SEC-ORCH-C-row-A', '001', 290.82, 326.59, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-A-s002', 'M200002-M100001-SEC-ORCH-C-row-A', '002', 295.9, 344.01, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-A-s003', 'M200002-M100001-SEC-ORCH-C-row-A', '003', 298.97, 361.89, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-A-s004', 'M200002-M100001-SEC-ORCH-C-row-A', '004', 300, 380, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-A-s005', 'M200002-M100001-SEC-ORCH-C-row-A', '005', 298.97, 398.11, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-A-s006', 'M200002-M100001-SEC-ORCH-C-row-A', '006', 295.9, 415.99, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-A-s007', 'M200002-M100001-SEC-ORCH-C-row-A', '007', 290.82, 433.41, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-B-s001', 'M200002-M100001-SEC-ORCH-C-row-B', '001', 307.79, 320.58, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-B-s002', 'M200002-M100001-SEC-ORCH-C-row-B', '002', 313.44, 339.96, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-B-s003', 'M200002-M100001-SEC-ORCH-C-row-B', '003', 316.86, 359.85, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-B-s004', 'M200002-M100001-SEC-ORCH-C-row-B', '004', 318, 380, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-B-s005', 'M200002-M100001-SEC-ORCH-C-row-B', '005', 316.86, 400.15, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-B-s006', 'M200002-M100001-SEC-ORCH-C-row-B', '006', 313.44, 420.04, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-B-s007', 'M200002-M100001-SEC-ORCH-C-row-B', '007', 307.79, 439.42, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-C-s001', 'M200002-M100001-SEC-ORCH-C-row-C', '001', 324.76, 314.57, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-C-s002', 'M200002-M100001-SEC-ORCH-C-row-C', '002', 330.98, 335.91, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-C-s003', 'M200002-M100001-SEC-ORCH-C-row-C', '003', 334.74, 357.81, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-C-s004', 'M200002-M100001-SEC-ORCH-C-row-C', '004', 336, 380, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-C-s005', 'M200002-M100001-SEC-ORCH-C-row-C', '005', 334.74, 402.19, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-C-s006', 'M200002-M100001-SEC-ORCH-C-row-C', '006', 330.98, 424.09, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-C-s007', 'M200002-M100001-SEC-ORCH-C-row-C', '007', 324.76, 445.43, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-D-s001', 'M200002-M100001-SEC-ORCH-C-row-D', '001', 341.73, 308.57, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-D-s002', 'M200002-M100001-SEC-ORCH-C-row-D', '002', 348.52, 331.86, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-D-s003', 'M200002-M100001-SEC-ORCH-C-row-D', '003', 352.62, 355.77, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-D-s004', 'M200002-M100001-SEC-ORCH-C-row-D', '004', 354, 380, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-D-s005', 'M200002-M100001-SEC-ORCH-C-row-D', '005', 352.62, 404.23, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-D-s006', 'M200002-M100001-SEC-ORCH-C-row-D', '006', 348.52, 428.14, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-D-s007', 'M200002-M100001-SEC-ORCH-C-row-D', '007', 341.73, 451.43, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-E-s001', 'M200002-M100001-SEC-ORCH-C-row-E', '001', 358.69, 302.56, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-E-s002', 'M200002-M100001-SEC-ORCH-C-row-E', '002', 366.05, 327.81, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-E-s003', 'M200002-M100001-SEC-ORCH-C-row-E', '003', 370.51, 353.74, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-E-s004', 'M200002-M100001-SEC-ORCH-C-row-E', '004', 372, 380, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-E-s005', 'M200002-M100001-SEC-ORCH-C-row-E', '005', 370.51, 406.26, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-E-s006', 'M200002-M100001-SEC-ORCH-C-row-E', '006', 366.05, 432.19, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-E-s007', 'M200002-M100001-SEC-ORCH-C-row-E', '007', 358.69, 457.44, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-F-s001', 'M200002-M100001-SEC-ORCH-C-row-F', '001', 375.66, 296.55, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-F-s002', 'M200002-M100001-SEC-ORCH-C-row-F', '002', 383.59, 323.76, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-F-s003', 'M200002-M100001-SEC-ORCH-C-row-F', '003', 388.39, 351.7, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-F-s004', 'M200002-M100001-SEC-ORCH-C-row-F', '004', 390, 380, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-F-s005', 'M200002-M100001-SEC-ORCH-C-row-F', '005', 388.39, 408.3, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-F-s006', 'M200002-M100001-SEC-ORCH-C-row-F', '006', 383.59, 436.24, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-C-row-F-s007', 'M200002-M100001-SEC-ORCH-C-row-F', '007', 375.66, 463.45, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-A-s001', 'M200002-M100001-SEC-ORCH-R-row-A', '001', 285.009245925864, 447.6189218785119, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-A-s002', 'M200002-M100001-SEC-ORCH-R-row-A', '002', 274.7923835211436, 466.2033256010981, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-A-s003', 'M200002-M100001-SEC-ORCH-R-row-A', '003', 262.2073645815428, 483.27323003583206, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-A-s004', 'M200002-M100001-SEC-ORCH-R-row-A', '004', 247.47529429167844, 498.52873540588, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-A-s005', 'M200002-M100001-SEC-ORCH-R-row-A', '005', 230.85499920849622, 511.7018189655107, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-A-s006', 'M200002-M100001-SEC-ORCH-R-row-A', '006', 212.63847995832748, 522.5610438701389, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-B-s001', 'M200002-M100001-SEC-ORCH-R-row-B', '001', 301.3227860925237, 455.2260505898445, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-B-s002', 'M200002-M100001-SEC-ORCH-R-row-B', '002', 289.95652666727227, 475.9011997312216, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-B-s003', 'M200002-M100001-SEC-ORCH-R-row-B', '003', 275.95569309696634, 494.8914684148632, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-B-s004', 'M200002-M100001-SEC-ORCH-R-row-B', '004', 259.56626489949224, 511.86321813904146, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-B-s005', 'M200002-M100001-SEC-ORCH-R-row-B', '005', 241.07618661945202, 526.5182735991307, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-B-s006', 'M200002-M100001-SEC-ORCH-R-row-B', '006', 220.81030895363932, 538.5991613055295, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-C-s001', 'M200002-M100001-SEC-ORCH-R-row-C', '001', 317.6363262591834, 462.8331793011771, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-C-s002', 'M200002-M100001-SEC-ORCH-R-row-C', '002', 307.41466254978303, 481.92316107412626, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-C-s003', 'M200002-M100001-SEC-ORCH-R-row-C', '003', 295.14951752730985, 499.76905781980156, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-C-s004', 'M200002-M100001-SEC-ORCH-R-row-C', '004', 280.99060086637564, 516.1530406099635, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-C-s005', 'M200002-M100001-SEC-ORCH-R-row-C', '005', 265.11073782116836, 530.875124794788, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-C-s006', 'M200002-M100001-SEC-ORCH-R-row-C', '006', 247.70375970187797, 543.7556110369355, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-D-s001', 'M200002-M100001-SEC-ORCH-R-row-D', '001', 333.9498664258431, 470.44030801250966, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-D-s002', 'M200002-M100001-SEC-ORCH-R-row-D', '002', 322.7894784982325, 491.2834513768521, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-D-s003', 'M200002-M100001-SEC-ORCH-R-row-D', '003', 309.3979426063486, 510.76825700733434, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-D-s004', 'M200002-M100001-SEC-ORCH-R-row-D', '004', 293.93871727247137, 528.6568912782254, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-D-s005', 'M200002-M100001-SEC-ORCH-R-row-D', '005', 276.6004994578062, 544.7310036024727, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-D-s006', 'M200002-M100001-SEC-ORCH-R-row-D', '006', 257.5949213071525, 558.7943916423684, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-E-s001', 'M200002-M100001-SEC-ORCH-R-row-E', '001', 350.26340659250275, 478.0474367238423, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-E-s002', 'M200002-M100001-SEC-ORCH-R-row-E', '002', 338.16429444668194, 500.643741679578, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-E-s003', 'M200002-M100001-SEC-ORCH-R-row-E', '003', 323.6463676853872, 521.7674561948671, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-E-s004', 'M200002-M100001-SEC-ORCH-R-row-E', '004', 306.8868336785671, 541.1607419464874, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-E-s005', 'M200002-M100001-SEC-ORCH-R-row-E', '005', 288.09026109444414, 558.5868824101573, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-E-s006', 'M200002-M100001-SEC-ORCH-R-row-E', '006', 267.486082912427, 573.8331722478013, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-E-s007', 'M200002-M100001-SEC-ORCH-R-row-E', '007', 245.32579593957487, 586.7135136117013, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-F-s001', 'M200002-M100001-SEC-ORCH-R-row-F', '001', 366.5769467591625, 485.65456543517485, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-F-s002', 'M200002-M100001-SEC-ORCH-R-row-F', '002', 353.5391103951314, 510.0040319823039, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-F-s003', 'M200002-M100001-SEC-ORCH-R-row-F', '003', 337.89479276442586, 532.7666553823999, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-F-s004', 'M200002-M100001-SEC-ORCH-R-row-F', '004', 319.8349500846628, 553.6645926147494, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-F-s005', 'M200002-M100001-SEC-ORCH-R-row-F', '005', 299.58002273108207, 572.442761217842, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-F-s006', 'M200002-M100001-SEC-ORCH-R-row-F', '006', 277.3772445177015, 588.8719528532341, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200002-M100001-SEC-ORCH-R-row-F-s007', 'M200002-M100001-SEC-ORCH-R-row-F', '007', 253.49762493488672, 602.7516310470919, 'AVAILABLE', NULL, 'M200002-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;

-- ================== SEATS FOR MANIFEST M200003 ==================
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200003-M100001-SEC-ORCH-L-row-A', 'M200003-M100001-SEC-ORCH-L', 'A')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200003-M100001-SEC-ORCH-L-row-B', 'M200003-M100001-SEC-ORCH-L', 'B')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200003-M100001-SEC-ORCH-L-row-C', 'M200003-M100001-SEC-ORCH-L', 'C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200003-M100001-SEC-ORCH-L-row-D', 'M200003-M100001-SEC-ORCH-L', 'D')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200003-M100001-SEC-ORCH-L-row-E', 'M200003-M100001-SEC-ORCH-L', 'E')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200003-M100001-SEC-ORCH-L-row-F', 'M200003-M100001-SEC-ORCH-L', 'F')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200003-M100001-SEC-ORCH-C-row-A', 'M200003-M100001-SEC-ORCH-C', 'A')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200003-M100001-SEC-ORCH-C-row-B', 'M200003-M100001-SEC-ORCH-C', 'B')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200003-M100001-SEC-ORCH-C-row-C', 'M200003-M100001-SEC-ORCH-C', 'C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200003-M100001-SEC-ORCH-C-row-D', 'M200003-M100001-SEC-ORCH-C', 'D')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200003-M100001-SEC-ORCH-C-row-E', 'M200003-M100001-SEC-ORCH-C', 'E')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200003-M100001-SEC-ORCH-C-row-F', 'M200003-M100001-SEC-ORCH-C', 'F')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200003-M100001-SEC-ORCH-R-row-A', 'M200003-M100001-SEC-ORCH-R', 'A')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200003-M100001-SEC-ORCH-R-row-B', 'M200003-M100001-SEC-ORCH-R', 'B')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200003-M100001-SEC-ORCH-R-row-C', 'M200003-M100001-SEC-ORCH-R', 'C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200003-M100001-SEC-ORCH-R-row-D', 'M200003-M100001-SEC-ORCH-R', 'D')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200003-M100001-SEC-ORCH-R-row-E', 'M200003-M100001-SEC-ORCH-R', 'E')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200003-M100001-SEC-ORCH-R-row-F', 'M200003-M100001-SEC-ORCH-R', 'F')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-A-s001', 'M200003-M100001-SEC-ORCH-L-row-A', '001', 212.63847995832748, 237.43895612986114, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-A-s002', 'M200003-M100001-SEC-ORCH-L-row-A', '002', 230.85499920849622, 248.29818103448932, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-A-s003', 'M200003-M100001-SEC-ORCH-L-row-A', '003', 247.47529429167844, 261.47126459412, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-A-s004', 'M200003-M100001-SEC-ORCH-L-row-A', '004', 262.2073645815428, 276.72676996416794, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-A-s005', 'M200003-M100001-SEC-ORCH-L-row-A', '005', 274.7923835211436, 293.7966743989019, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-A-s006', 'M200003-M100001-SEC-ORCH-L-row-A', '006', 285.009245925864, 312.3810781214881, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-B-s001', 'M200003-M100001-SEC-ORCH-L-row-B', '001', 220.81030895363932, 221.40083869447054, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-B-s002', 'M200003-M100001-SEC-ORCH-L-row-B', '002', 241.07618661945202, 233.48172640086935, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-B-s003', 'M200003-M100001-SEC-ORCH-L-row-B', '003', 259.56626489949224, 248.1367818609585, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-B-s004', 'M200003-M100001-SEC-ORCH-L-row-B', '004', 275.95569309696634, 265.1085315851368, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-B-s005', 'M200003-M100001-SEC-ORCH-L-row-B', '005', 289.95652666727227, 284.0988002687784, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-B-s006', 'M200003-M100001-SEC-ORCH-L-row-B', '006', 301.3227860925237, 304.7739494101555, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-C-s001', 'M200003-M100001-SEC-ORCH-L-row-C', '001', 228.9821379489512, 205.3627212590799, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-C-s002', 'M200003-M100001-SEC-ORCH-L-row-C', '002', 247.703759701878, 216.24438896306447, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-C-s003', 'M200003-M100001-SEC-ORCH-L-row-C', '003', 265.11073782116836, 229.1248752052119, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-C-s004', 'M200003-M100001-SEC-ORCH-L-row-C', '004', 280.99060086637564, 243.84695939003655, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-C-s005', 'M200003-M100001-SEC-ORCH-L-row-C', '005', 295.14951752730985, 260.23094218019844, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-C-s006', 'M200003-M100001-SEC-ORCH-L-row-C', '006', 307.41466254978303, 278.07683892587374, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-D-s001', 'M200003-M100001-SEC-ORCH-L-row-D', '001', 237.15396694426303, 189.3246038236893, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-D-s002', 'M200003-M100001-SEC-ORCH-L-row-D', '002', 257.5949213071525, 201.2056083576316, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-D-s003', 'M200003-M100001-SEC-ORCH-L-row-D', '003', 276.6004994578062, 215.26899639752727, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-D-s004', 'M200003-M100001-SEC-ORCH-L-row-D', '004', 293.93871727247137, 231.3431087217746, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-D-s005', 'M200003-M100001-SEC-ORCH-L-row-D', '005', 309.3979426063485, 249.23174299266563, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-D-s006', 'M200003-M100001-SEC-ORCH-L-row-D', '006', 322.7894784982325, 268.7165486231479, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-E-s001', 'M200003-M100001-SEC-ORCH-L-row-E', '001', 245.32579593957487, 173.28648638829867, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-E-s002', 'M200003-M100001-SEC-ORCH-L-row-E', '002', 267.48608291242704, 186.16682775219877, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-E-s003', 'M200003-M100001-SEC-ORCH-L-row-E', '003', 288.09026109444414, 201.41311758984267, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-E-s004', 'M200003-M100001-SEC-ORCH-L-row-E', '004', 306.8868336785671, 218.83925805351265, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-E-s005', 'M200003-M100001-SEC-ORCH-L-row-E', '005', 323.6463676853872, 238.23254380513282, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-E-s006', 'M200003-M100001-SEC-ORCH-L-row-E', '006', 338.16429444668194, 259.356258320422, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-E-s007', 'M200003-M100001-SEC-ORCH-L-row-E', '007', 350.26340659250275, 281.9525632761577, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-F-s001', 'M200003-M100001-SEC-ORCH-L-row-F', '001', 253.49762493488672, 157.24836895290807, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-F-s002', 'M200003-M100001-SEC-ORCH-L-row-F', '002', 277.37724451770157, 171.1280471467659, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-F-s003', 'M200003-M100001-SEC-ORCH-L-row-F', '003', 299.58002273108207, 187.55723878215804, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-F-s004', 'M200003-M100001-SEC-ORCH-L-row-F', '004', 319.8349500846628, 206.33540738525068, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-F-s005', 'M200003-M100001-SEC-ORCH-L-row-F', '005', 337.8947927644258, 227.23334461760004, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-F-s006', 'M200003-M100001-SEC-ORCH-L-row-F', '006', 353.5391103951314, 249.99596801769613, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-L-row-F-s007', 'M200003-M100001-SEC-ORCH-L-row-F', '007', 366.5769467591625, 274.34543456482515, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-A-s001', 'M200003-M100001-SEC-ORCH-C-row-A', '001', 290.82, 326.59, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-A-s002', 'M200003-M100001-SEC-ORCH-C-row-A', '002', 295.9, 344.01, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-A-s003', 'M200003-M100001-SEC-ORCH-C-row-A', '003', 298.97, 361.89, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-A-s004', 'M200003-M100001-SEC-ORCH-C-row-A', '004', 300, 380, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-A-s005', 'M200003-M100001-SEC-ORCH-C-row-A', '005', 298.97, 398.11, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-A-s006', 'M200003-M100001-SEC-ORCH-C-row-A', '006', 295.9, 415.99, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-A-s007', 'M200003-M100001-SEC-ORCH-C-row-A', '007', 290.82, 433.41, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-B-s001', 'M200003-M100001-SEC-ORCH-C-row-B', '001', 307.79, 320.58, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-B-s002', 'M200003-M100001-SEC-ORCH-C-row-B', '002', 313.44, 339.96, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-B-s003', 'M200003-M100001-SEC-ORCH-C-row-B', '003', 316.86, 359.85, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-B-s004', 'M200003-M100001-SEC-ORCH-C-row-B', '004', 318, 380, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-B-s005', 'M200003-M100001-SEC-ORCH-C-row-B', '005', 316.86, 400.15, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-B-s006', 'M200003-M100001-SEC-ORCH-C-row-B', '006', 313.44, 420.04, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-B-s007', 'M200003-M100001-SEC-ORCH-C-row-B', '007', 307.79, 439.42, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-C-s001', 'M200003-M100001-SEC-ORCH-C-row-C', '001', 324.76, 314.57, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-C-s002', 'M200003-M100001-SEC-ORCH-C-row-C', '002', 330.98, 335.91, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-C-s003', 'M200003-M100001-SEC-ORCH-C-row-C', '003', 334.74, 357.81, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-C-s004', 'M200003-M100001-SEC-ORCH-C-row-C', '004', 336, 380, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-C-s005', 'M200003-M100001-SEC-ORCH-C-row-C', '005', 334.74, 402.19, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-C-s006', 'M200003-M100001-SEC-ORCH-C-row-C', '006', 330.98, 424.09, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-C-s007', 'M200003-M100001-SEC-ORCH-C-row-C', '007', 324.76, 445.43, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-D-s001', 'M200003-M100001-SEC-ORCH-C-row-D', '001', 341.73, 308.57, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-D-s002', 'M200003-M100001-SEC-ORCH-C-row-D', '002', 348.52, 331.86, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-D-s003', 'M200003-M100001-SEC-ORCH-C-row-D', '003', 352.62, 355.77, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-D-s004', 'M200003-M100001-SEC-ORCH-C-row-D', '004', 354, 380, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-D-s005', 'M200003-M100001-SEC-ORCH-C-row-D', '005', 352.62, 404.23, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-D-s006', 'M200003-M100001-SEC-ORCH-C-row-D', '006', 348.52, 428.14, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-D-s007', 'M200003-M100001-SEC-ORCH-C-row-D', '007', 341.73, 451.43, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-E-s001', 'M200003-M100001-SEC-ORCH-C-row-E', '001', 358.69, 302.56, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-E-s002', 'M200003-M100001-SEC-ORCH-C-row-E', '002', 366.05, 327.81, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-E-s003', 'M200003-M100001-SEC-ORCH-C-row-E', '003', 370.51, 353.74, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-E-s004', 'M200003-M100001-SEC-ORCH-C-row-E', '004', 372, 380, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-E-s005', 'M200003-M100001-SEC-ORCH-C-row-E', '005', 370.51, 406.26, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-E-s006', 'M200003-M100001-SEC-ORCH-C-row-E', '006', 366.05, 432.19, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-E-s007', 'M200003-M100001-SEC-ORCH-C-row-E', '007', 358.69, 457.44, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-F-s001', 'M200003-M100001-SEC-ORCH-C-row-F', '001', 375.66, 296.55, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-F-s002', 'M200003-M100001-SEC-ORCH-C-row-F', '002', 383.59, 323.76, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-F-s003', 'M200003-M100001-SEC-ORCH-C-row-F', '003', 388.39, 351.7, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-F-s004', 'M200003-M100001-SEC-ORCH-C-row-F', '004', 390, 380, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-F-s005', 'M200003-M100001-SEC-ORCH-C-row-F', '005', 388.39, 408.3, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-F-s006', 'M200003-M100001-SEC-ORCH-C-row-F', '006', 383.59, 436.24, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-C-row-F-s007', 'M200003-M100001-SEC-ORCH-C-row-F', '007', 375.66, 463.45, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-A-s001', 'M200003-M100001-SEC-ORCH-R-row-A', '001', 285.009245925864, 447.6189218785119, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-A-s002', 'M200003-M100001-SEC-ORCH-R-row-A', '002', 274.7923835211436, 466.2033256010981, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-A-s003', 'M200003-M100001-SEC-ORCH-R-row-A', '003', 262.2073645815428, 483.27323003583206, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-A-s004', 'M200003-M100001-SEC-ORCH-R-row-A', '004', 247.47529429167844, 498.52873540588, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-A-s005', 'M200003-M100001-SEC-ORCH-R-row-A', '005', 230.85499920849622, 511.7018189655107, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-A-s006', 'M200003-M100001-SEC-ORCH-R-row-A', '006', 212.63847995832748, 522.5610438701389, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-B-s001', 'M200003-M100001-SEC-ORCH-R-row-B', '001', 301.3227860925237, 455.2260505898445, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-B-s002', 'M200003-M100001-SEC-ORCH-R-row-B', '002', 289.95652666727227, 475.9011997312216, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-B-s003', 'M200003-M100001-SEC-ORCH-R-row-B', '003', 275.95569309696634, 494.8914684148632, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-B-s004', 'M200003-M100001-SEC-ORCH-R-row-B', '004', 259.56626489949224, 511.86321813904146, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-B-s005', 'M200003-M100001-SEC-ORCH-R-row-B', '005', 241.07618661945202, 526.5182735991307, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-B-s006', 'M200003-M100001-SEC-ORCH-R-row-B', '006', 220.81030895363932, 538.5991613055295, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-C-s001', 'M200003-M100001-SEC-ORCH-R-row-C', '001', 317.6363262591834, 462.8331793011771, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-C-s002', 'M200003-M100001-SEC-ORCH-R-row-C', '002', 307.41466254978303, 481.92316107412626, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-C-s003', 'M200003-M100001-SEC-ORCH-R-row-C', '003', 295.14951752730985, 499.76905781980156, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-C-s004', 'M200003-M100001-SEC-ORCH-R-row-C', '004', 280.99060086637564, 516.1530406099635, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-C-s005', 'M200003-M100001-SEC-ORCH-R-row-C', '005', 265.11073782116836, 530.875124794788, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-C-s006', 'M200003-M100001-SEC-ORCH-R-row-C', '006', 247.70375970187797, 543.7556110369355, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-D-s001', 'M200003-M100001-SEC-ORCH-R-row-D', '001', 333.9498664258431, 470.44030801250966, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-D-s002', 'M200003-M100001-SEC-ORCH-R-row-D', '002', 322.7894784982325, 491.2834513768521, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-D-s003', 'M200003-M100001-SEC-ORCH-R-row-D', '003', 309.3979426063486, 510.76825700733434, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-D-s004', 'M200003-M100001-SEC-ORCH-R-row-D', '004', 293.93871727247137, 528.6568912782254, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-D-s005', 'M200003-M100001-SEC-ORCH-R-row-D', '005', 276.6004994578062, 544.7310036024727, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-D-s006', 'M200003-M100001-SEC-ORCH-R-row-D', '006', 257.5949213071525, 558.7943916423684, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-E-s001', 'M200003-M100001-SEC-ORCH-R-row-E', '001', 350.26340659250275, 478.0474367238423, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-E-s002', 'M200003-M100001-SEC-ORCH-R-row-E', '002', 338.16429444668194, 500.643741679578, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-E-s003', 'M200003-M100001-SEC-ORCH-R-row-E', '003', 323.6463676853872, 521.7674561948671, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-E-s004', 'M200003-M100001-SEC-ORCH-R-row-E', '004', 306.8868336785671, 541.1607419464874, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-E-s005', 'M200003-M100001-SEC-ORCH-R-row-E', '005', 288.09026109444414, 558.5868824101573, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-E-s006', 'M200003-M100001-SEC-ORCH-R-row-E', '006', 267.486082912427, 573.8331722478013, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-E-s007', 'M200003-M100001-SEC-ORCH-R-row-E', '007', 245.32579593957487, 586.7135136117013, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-F-s001', 'M200003-M100001-SEC-ORCH-R-row-F', '001', 366.5769467591625, 485.65456543517485, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-F-s002', 'M200003-M100001-SEC-ORCH-R-row-F', '002', 353.5391103951314, 510.0040319823039, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-F-s003', 'M200003-M100001-SEC-ORCH-R-row-F', '003', 337.89479276442586, 532.7666553823999, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-F-s004', 'M200003-M100001-SEC-ORCH-R-row-F', '004', 319.8349500846628, 553.6645926147494, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-F-s005', 'M200003-M100001-SEC-ORCH-R-row-F', '005', 299.58002273108207, 572.442761217842, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-F-s006', 'M200003-M100001-SEC-ORCH-R-row-F', '006', 277.3772445177015, 588.8719528532341, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200003-M100001-SEC-ORCH-R-row-F-s007', 'M200003-M100001-SEC-ORCH-R-row-F', '007', 253.49762493488672, 602.7516310470919, 'AVAILABLE', NULL, 'M200003-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;

-- ================== SEATS FOR MANIFEST M200004 ==================
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200004-M100001-SEC-ORCH-L-row-A', 'M200004-M100001-SEC-ORCH-L', 'A')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200004-M100001-SEC-ORCH-L-row-B', 'M200004-M100001-SEC-ORCH-L', 'B')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200004-M100001-SEC-ORCH-L-row-C', 'M200004-M100001-SEC-ORCH-L', 'C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200004-M100001-SEC-ORCH-L-row-D', 'M200004-M100001-SEC-ORCH-L', 'D')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200004-M100001-SEC-ORCH-L-row-E', 'M200004-M100001-SEC-ORCH-L', 'E')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200004-M100001-SEC-ORCH-L-row-F', 'M200004-M100001-SEC-ORCH-L', 'F')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200004-M100001-SEC-ORCH-C-row-A', 'M200004-M100001-SEC-ORCH-C', 'A')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200004-M100001-SEC-ORCH-C-row-B', 'M200004-M100001-SEC-ORCH-C', 'B')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200004-M100001-SEC-ORCH-C-row-C', 'M200004-M100001-SEC-ORCH-C', 'C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200004-M100001-SEC-ORCH-C-row-D', 'M200004-M100001-SEC-ORCH-C', 'D')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200004-M100001-SEC-ORCH-C-row-E', 'M200004-M100001-SEC-ORCH-C', 'E')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200004-M100001-SEC-ORCH-C-row-F', 'M200004-M100001-SEC-ORCH-C', 'F')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200004-M100001-SEC-ORCH-R-row-A', 'M200004-M100001-SEC-ORCH-R', 'A')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200004-M100001-SEC-ORCH-R-row-B', 'M200004-M100001-SEC-ORCH-R', 'B')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200004-M100001-SEC-ORCH-R-row-C', 'M200004-M100001-SEC-ORCH-R', 'C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200004-M100001-SEC-ORCH-R-row-D', 'M200004-M100001-SEC-ORCH-R', 'D')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200004-M100001-SEC-ORCH-R-row-E', 'M200004-M100001-SEC-ORCH-R', 'E')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat_row (id, section_id, name)
VALUES ('M200004-M100001-SEC-ORCH-R-row-F', 'M200004-M100001-SEC-ORCH-R', 'F')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-A-s001', 'M200004-M100001-SEC-ORCH-L-row-A', '001', 212.63847995832748, 237.43895612986114, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-A-s002', 'M200004-M100001-SEC-ORCH-L-row-A', '002', 230.85499920849622, 248.29818103448932, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-A-s003', 'M200004-M100001-SEC-ORCH-L-row-A', '003', 247.47529429167844, 261.47126459412, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-A-s004', 'M200004-M100001-SEC-ORCH-L-row-A', '004', 262.2073645815428, 276.72676996416794, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-A-s005', 'M200004-M100001-SEC-ORCH-L-row-A', '005', 274.7923835211436, 293.7966743989019, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-A-s006', 'M200004-M100001-SEC-ORCH-L-row-A', '006', 285.009245925864, 312.3810781214881, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-B-s001', 'M200004-M100001-SEC-ORCH-L-row-B', '001', 220.81030895363932, 221.40083869447054, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-B-s002', 'M200004-M100001-SEC-ORCH-L-row-B', '002', 241.07618661945202, 233.48172640086935, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-B-s003', 'M200004-M100001-SEC-ORCH-L-row-B', '003', 259.56626489949224, 248.1367818609585, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-B-s004', 'M200004-M100001-SEC-ORCH-L-row-B', '004', 275.95569309696634, 265.1085315851368, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-B-s005', 'M200004-M100001-SEC-ORCH-L-row-B', '005', 289.95652666727227, 284.0988002687784, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-B-s006', 'M200004-M100001-SEC-ORCH-L-row-B', '006', 301.3227860925237, 304.7739494101555, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-C-s001', 'M200004-M100001-SEC-ORCH-L-row-C', '001', 228.9821379489512, 205.3627212590799, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-C-s002', 'M200004-M100001-SEC-ORCH-L-row-C', '002', 247.703759701878, 216.24438896306447, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-C-s003', 'M200004-M100001-SEC-ORCH-L-row-C', '003', 265.11073782116836, 229.1248752052119, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-C-s004', 'M200004-M100001-SEC-ORCH-L-row-C', '004', 280.99060086637564, 243.84695939003655, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-C-s005', 'M200004-M100001-SEC-ORCH-L-row-C', '005', 295.14951752730985, 260.23094218019844, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-C-s006', 'M200004-M100001-SEC-ORCH-L-row-C', '006', 307.41466254978303, 278.07683892587374, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-D-s001', 'M200004-M100001-SEC-ORCH-L-row-D', '001', 237.15396694426303, 189.3246038236893, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-D-s002', 'M200004-M100001-SEC-ORCH-L-row-D', '002', 257.5949213071525, 201.2056083576316, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-D-s003', 'M200004-M100001-SEC-ORCH-L-row-D', '003', 276.6004994578062, 215.26899639752727, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-D-s004', 'M200004-M100001-SEC-ORCH-L-row-D', '004', 293.93871727247137, 231.3431087217746, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-D-s005', 'M200004-M100001-SEC-ORCH-L-row-D', '005', 309.3979426063485, 249.23174299266563, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-D-s006', 'M200004-M100001-SEC-ORCH-L-row-D', '006', 322.7894784982325, 268.7165486231479, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-E-s001', 'M200004-M100001-SEC-ORCH-L-row-E', '001', 245.32579593957487, 173.28648638829867, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-E-s002', 'M200004-M100001-SEC-ORCH-L-row-E', '002', 267.48608291242704, 186.16682775219877, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-E-s003', 'M200004-M100001-SEC-ORCH-L-row-E', '003', 288.09026109444414, 201.41311758984267, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-E-s004', 'M200004-M100001-SEC-ORCH-L-row-E', '004', 306.8868336785671, 218.83925805351265, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-E-s005', 'M200004-M100001-SEC-ORCH-L-row-E', '005', 323.6463676853872, 238.23254380513282, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-E-s006', 'M200004-M100001-SEC-ORCH-L-row-E', '006', 338.16429444668194, 259.356258320422, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-E-s007', 'M200004-M100001-SEC-ORCH-L-row-E', '007', 350.26340659250275, 281.9525632761577, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-F-s001', 'M200004-M100001-SEC-ORCH-L-row-F', '001', 253.49762493488672, 157.24836895290807, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-F-s002', 'M200004-M100001-SEC-ORCH-L-row-F', '002', 277.37724451770157, 171.1280471467659, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-F-s003', 'M200004-M100001-SEC-ORCH-L-row-F', '003', 299.58002273108207, 187.55723878215804, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-F-s004', 'M200004-M100001-SEC-ORCH-L-row-F', '004', 319.8349500846628, 206.33540738525068, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-F-s005', 'M200004-M100001-SEC-ORCH-L-row-F', '005', 337.8947927644258, 227.23334461760004, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-F-s006', 'M200004-M100001-SEC-ORCH-L-row-F', '006', 353.5391103951314, 249.99596801769613, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-L-row-F-s007', 'M200004-M100001-SEC-ORCH-L-row-F', '007', 366.5769467591625, 274.34543456482515, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-L')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-A-s001', 'M200004-M100001-SEC-ORCH-C-row-A', '001', 290.82, 326.59, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-A-s002', 'M200004-M100001-SEC-ORCH-C-row-A', '002', 295.9, 344.01, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-A-s003', 'M200004-M100001-SEC-ORCH-C-row-A', '003', 298.97, 361.89, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-A-s004', 'M200004-M100001-SEC-ORCH-C-row-A', '004', 300, 380, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-A-s005', 'M200004-M100001-SEC-ORCH-C-row-A', '005', 298.97, 398.11, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-A-s006', 'M200004-M100001-SEC-ORCH-C-row-A', '006', 295.9, 415.99, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-A-s007', 'M200004-M100001-SEC-ORCH-C-row-A', '007', 290.82, 433.41, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-B-s001', 'M200004-M100001-SEC-ORCH-C-row-B', '001', 307.79, 320.58, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-B-s002', 'M200004-M100001-SEC-ORCH-C-row-B', '002', 313.44, 339.96, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-B-s003', 'M200004-M100001-SEC-ORCH-C-row-B', '003', 316.86, 359.85, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-B-s004', 'M200004-M100001-SEC-ORCH-C-row-B', '004', 318, 380, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-B-s005', 'M200004-M100001-SEC-ORCH-C-row-B', '005', 316.86, 400.15, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-B-s006', 'M200004-M100001-SEC-ORCH-C-row-B', '006', 313.44, 420.04, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-B-s007', 'M200004-M100001-SEC-ORCH-C-row-B', '007', 307.79, 439.42, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-C-s001', 'M200004-M100001-SEC-ORCH-C-row-C', '001', 324.76, 314.57, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-C-s002', 'M200004-M100001-SEC-ORCH-C-row-C', '002', 330.98, 335.91, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-C-s003', 'M200004-M100001-SEC-ORCH-C-row-C', '003', 334.74, 357.81, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-C-s004', 'M200004-M100001-SEC-ORCH-C-row-C', '004', 336, 380, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-C-s005', 'M200004-M100001-SEC-ORCH-C-row-C', '005', 334.74, 402.19, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-C-s006', 'M200004-M100001-SEC-ORCH-C-row-C', '006', 330.98, 424.09, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-C-s007', 'M200004-M100001-SEC-ORCH-C-row-C', '007', 324.76, 445.43, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-D-s001', 'M200004-M100001-SEC-ORCH-C-row-D', '001', 341.73, 308.57, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-D-s002', 'M200004-M100001-SEC-ORCH-C-row-D', '002', 348.52, 331.86, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-D-s003', 'M200004-M100001-SEC-ORCH-C-row-D', '003', 352.62, 355.77, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-D-s004', 'M200004-M100001-SEC-ORCH-C-row-D', '004', 354, 380, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-D-s005', 'M200004-M100001-SEC-ORCH-C-row-D', '005', 352.62, 404.23, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-D-s006', 'M200004-M100001-SEC-ORCH-C-row-D', '006', 348.52, 428.14, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-D-s007', 'M200004-M100001-SEC-ORCH-C-row-D', '007', 341.73, 451.43, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-E-s001', 'M200004-M100001-SEC-ORCH-C-row-E', '001', 358.69, 302.56, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-E-s002', 'M200004-M100001-SEC-ORCH-C-row-E', '002', 366.05, 327.81, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-E-s003', 'M200004-M100001-SEC-ORCH-C-row-E', '003', 370.51, 353.74, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-E-s004', 'M200004-M100001-SEC-ORCH-C-row-E', '004', 372, 380, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-E-s005', 'M200004-M100001-SEC-ORCH-C-row-E', '005', 370.51, 406.26, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-E-s006', 'M200004-M100001-SEC-ORCH-C-row-E', '006', 366.05, 432.19, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-E-s007', 'M200004-M100001-SEC-ORCH-C-row-E', '007', 358.69, 457.44, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-F-s001', 'M200004-M100001-SEC-ORCH-C-row-F', '001', 375.66, 296.55, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-F-s002', 'M200004-M100001-SEC-ORCH-C-row-F', '002', 383.59, 323.76, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-F-s003', 'M200004-M100001-SEC-ORCH-C-row-F', '003', 388.39, 351.7, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-F-s004', 'M200004-M100001-SEC-ORCH-C-row-F', '004', 390, 380, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-F-s005', 'M200004-M100001-SEC-ORCH-C-row-F', '005', 388.39, 408.3, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-F-s006', 'M200004-M100001-SEC-ORCH-C-row-F', '006', 383.59, 436.24, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-C-row-F-s007', 'M200004-M100001-SEC-ORCH-C-row-F', '007', 375.66, 463.45, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-C')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-A-s001', 'M200004-M100001-SEC-ORCH-R-row-A', '001', 285.009245925864, 447.6189218785119, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-A-s002', 'M200004-M100001-SEC-ORCH-R-row-A', '002', 274.7923835211436, 466.2033256010981, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-A-s003', 'M200004-M100001-SEC-ORCH-R-row-A', '003', 262.2073645815428, 483.27323003583206, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-A-s004', 'M200004-M100001-SEC-ORCH-R-row-A', '004', 247.47529429167844, 498.52873540588, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-A-s005', 'M200004-M100001-SEC-ORCH-R-row-A', '005', 230.85499920849622, 511.7018189655107, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-A-s006', 'M200004-M100001-SEC-ORCH-R-row-A', '006', 212.63847995832748, 522.5610438701389, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-B-s001', 'M200004-M100001-SEC-ORCH-R-row-B', '001', 301.3227860925237, 455.2260505898445, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-B-s002', 'M200004-M100001-SEC-ORCH-R-row-B', '002', 289.95652666727227, 475.9011997312216, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-B-s003', 'M200004-M100001-SEC-ORCH-R-row-B', '003', 275.95569309696634, 494.8914684148632, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-B-s004', 'M200004-M100001-SEC-ORCH-R-row-B', '004', 259.56626489949224, 511.86321813904146, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-B-s005', 'M200004-M100001-SEC-ORCH-R-row-B', '005', 241.07618661945202, 526.5182735991307, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-B-s006', 'M200004-M100001-SEC-ORCH-R-row-B', '006', 220.81030895363932, 538.5991613055295, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-C-s001', 'M200004-M100001-SEC-ORCH-R-row-C', '001', 317.6363262591834, 462.8331793011771, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-C-s002', 'M200004-M100001-SEC-ORCH-R-row-C', '002', 307.41466254978303, 481.92316107412626, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-C-s003', 'M200004-M100001-SEC-ORCH-R-row-C', '003', 295.14951752730985, 499.76905781980156, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-C-s004', 'M200004-M100001-SEC-ORCH-R-row-C', '004', 280.99060086637564, 516.1530406099635, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-C-s005', 'M200004-M100001-SEC-ORCH-R-row-C', '005', 265.11073782116836, 530.875124794788, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-C-s006', 'M200004-M100001-SEC-ORCH-R-row-C', '006', 247.70375970187797, 543.7556110369355, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-D-s001', 'M200004-M100001-SEC-ORCH-R-row-D', '001', 333.9498664258431, 470.44030801250966, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-D-s002', 'M200004-M100001-SEC-ORCH-R-row-D', '002', 322.7894784982325, 491.2834513768521, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-D-s003', 'M200004-M100001-SEC-ORCH-R-row-D', '003', 309.3979426063486, 510.76825700733434, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-D-s004', 'M200004-M100001-SEC-ORCH-R-row-D', '004', 293.93871727247137, 528.6568912782254, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-D-s005', 'M200004-M100001-SEC-ORCH-R-row-D', '005', 276.6004994578062, 544.7310036024727, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-D-s006', 'M200004-M100001-SEC-ORCH-R-row-D', '006', 257.5949213071525, 558.7943916423684, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-E-s001', 'M200004-M100001-SEC-ORCH-R-row-E', '001', 350.26340659250275, 478.0474367238423, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-E-s002', 'M200004-M100001-SEC-ORCH-R-row-E', '002', 338.16429444668194, 500.643741679578, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-E-s003', 'M200004-M100001-SEC-ORCH-R-row-E', '003', 323.6463676853872, 521.7674561948671, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-E-s004', 'M200004-M100001-SEC-ORCH-R-row-E', '004', 306.8868336785671, 541.1607419464874, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-E-s005', 'M200004-M100001-SEC-ORCH-R-row-E', '005', 288.09026109444414, 558.5868824101573, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-E-s006', 'M200004-M100001-SEC-ORCH-R-row-E', '006', 267.486082912427, 573.8331722478013, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-E-s007', 'M200004-M100001-SEC-ORCH-R-row-E', '007', 245.32579593957487, 586.7135136117013, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-F-s001', 'M200004-M100001-SEC-ORCH-R-row-F', '001', 366.5769467591625, 485.65456543517485, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-F-s002', 'M200004-M100001-SEC-ORCH-R-row-F', '002', 353.5391103951314, 510.0040319823039, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-F-s003', 'M200004-M100001-SEC-ORCH-R-row-F', '003', 337.89479276442586, 532.7666553823999, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-F-s004', 'M200004-M100001-SEC-ORCH-R-row-F', '004', 319.8349500846628, 553.6645926147494, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-F-s005', 'M200004-M100001-SEC-ORCH-R-row-F', '005', 299.58002273108207, 572.442761217842, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-F-s006', 'M200004-M100001-SEC-ORCH-R-row-F', '006', 277.3772445177015, 588.8719528532341, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;
INSERT INTO seat (id, row_id, name, position_x, position_y, status, price_level_id, section_id)
VALUES ('M200004-M100001-SEC-ORCH-R-row-F-s007', 'M200004-M100001-SEC-ORCH-R-row-F', '007', 253.49762493488672, 602.7516310470919, 'AVAILABLE', NULL, 'M200004-M100001-SEC-ORCH-R')
ON CONFLICT (id) DO NOTHING;

UPDATE section s SET capacity = (SELECT COUNT(*) FROM seat WHERE section_id = s.id) WHERE type = 'RS';

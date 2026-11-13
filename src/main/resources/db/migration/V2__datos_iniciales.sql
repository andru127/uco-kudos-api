-- Categorías mínimas exigidas por el enunciado
INSERT INTO categories (id, name, description, active) VALUES
('897d8790-e54c-5c0c-ae2f-f9008c1e19dd', 'Rendimiento Académico', 'Desempeño académico sobresaliente', TRUE),
('52f68aa5-02f7-5f89-93a4-c39ec63819c3', 'Mejor Investigador', 'Aportes destacados a la investigación', TRUE),
('fad0e831-e4bc-5e47-a31f-d65dcfbdef82', 'Empatía', 'Capacidad de comprender y apoyar a los demás', TRUE),
('21d04070-99a6-5f65-a4bf-46fa2d6934dd', 'Comunicación Asertiva', 'Comunicación clara y respetuosa', TRUE),
('fbb8701b-f15a-5c0e-bce5-960501f20af3', 'Orientación al Logro', 'Enfoque en resultados y metas', TRUE),
('f2456ceb-880f-5188-883a-0377d2f56bf4', 'Trabajo en Equipo', 'Colaboración efectiva con otros', TRUE),
('51b56123-28a6-5d9c-bd0e-2db3f15ad29d', 'Trabajo Bajo Presión', 'Buen desempeño en situaciones exigentes', TRUE),
('f5e1efb6-87a0-55dd-8ba4-2358cb421eb8', 'Visión Estratégica', 'Capacidad de ver el panorama completo', TRUE),
('ec9b5466-5d7c-5239-bb27-07a6ad4de485', 'Innovación', 'Ideas y soluciones nuevas', TRUE),
('a1588b1a-5b75-510c-a8a7-90b2192b5c4a', 'Liderazgo', 'Inspira y guía a otros', TRUE),
('fdf8a3b9-4970-5b49-ba6b-87c779c5b375', 'Transformación Digital', 'Impulsa el uso de la tecnología', TRUE),
('0af93ef2-853c-5cc5-bf6e-3aa917cd0917', 'Mentor Inspirador', 'Acompaña y forma a otros', TRUE),
('e74b04b0-1aa0-5863-b9f5-5cc9cb8e2978', 'Espíritu UCO', 'Vive los valores institucionales', TRUE),
('500f378f-819d-5cca-851a-bcd7941e82e9', 'Servicio a la Comunidad', 'Aporta al bienestar de la comunidad', TRUE),
('1c00b958-07f3-5d78-a279-a446191ec1dc', 'Compromiso Institucional', 'Dedicación con la Universidad', TRUE);

INSERT INTO faculties (id, name) VALUES
('fc20abcf-11d9-5253-8974-6f5f9f2a387f', 'Facultad de Ingeniería');

INSERT INTO programs (id, faculty_id, name) VALUES
('d2581a5c-1944-5017-80df-795c5ba437d4', 'fc20abcf-11d9-5253-8974-6f5f9f2a387f', 'Ingeniería de Sistemas'),
('af1862e1-9deb-5a08-aa17-de3534f3b7ab', 'fc20abcf-11d9-5253-8974-6f5f9f2a387f', 'Ingeniería Industrial');

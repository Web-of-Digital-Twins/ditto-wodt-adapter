## [1.1.2](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/compare/1.1.1...1.1.2) (2024-12-12)

### Bug Fixes

* set dt see other location correctly when dereferenced from its uri ([99e4b11](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/99e4b1158443f28ce00fa689d7c0dc6caab78727))
* set relative urls correctly ([59a1f6b](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/59a1f6bc1206b5d3d0eb882909abbf186e7c3bef))
* set the dtd link header correctly with respect to dtkg url ([103a4c3](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/103a4c3437067513bf80d874784f72b3097c35c2))

### General maintenance

* add uri utils ([a1e9c78](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/a1e9c785425dc6c4d5a98d8ec62657cb43ec3ad8))

## [1.1.1](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/compare/1.1.0...1.1.1) (2024-12-02)

### Bug Fixes

* correct entry point in build ([1efe31d](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/1efe31d934dae8a35052d37347461cb3de54cce6))

## [1.1.0](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/compare/1.0.1...1.1.0) (2024-11-30)

### Features

* align with new platform registration notification ([c25061a](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/c25061a924b5a90da1049d7d101c8367252eeed9))

### Refactoring

* move to new package ([191073b](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/191073b3a01c1206492fe327e69895547388e631))

## [1.0.1](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/compare/1.0.0...1.0.1) (2024-10-28)

### Bug Fixes

* correct check of environment variables ([023b3bf](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/023b3bf8c66eaadd5061992df1386e8c5a734774))

### General maintenance

* add ontology configuration example ([3df80da](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/3df80da16ffe4caaf48d5c8e4d16e532c30bdd1e))
* update README ([7d7d871](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/7d7d87176bfb1a3abc5cb4590f4ba6a4f306b90b))

## 1.0.0 (2024-10-27)

### ⚠ BREAKING CHANGES

* align with dtd 1.0.0

### Features

* align with dtd 1.0.0 ([2a3acb1](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/2a3acb1bf57d9aca16794549e8096572e2bafdb7))

### Dependency updates

* **deps:** add ditto wot model dependency ([c72e01f](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/c72e01f3e99621bb5fc6ba3574cc85eb9686bd78))
* **deps:** include slf4j logging ([511a356](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/511a356ab588b738256dd79bbe1fde12e11c521e))

### Bug Fixes

* correct url resolution ([5ac6a7f](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/5ac6a7f9e7cd940bc6caff1e3c29aa049393efa9))
* correctly handle updates and creations of ditto elements ([b37e81f](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/b37e81fae7cc75c100cd5459094a44bec50d2385))

### Documentation

* typo in documentation ([c28f6fc](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/c28f6fc580dc0d2c444176533134fc921a2bf0c6))

### Build and continuous integration

* configure ci for building and release ([5c3c039](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/5c3c039ed573f3c76d20e2f3c82a650a5c68d4d3))
* delete check on forks ([91d8857](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/91d885731db61e0fe37a802c449d3a9663baa9aa))
* migrate from maven to gradle ([2d243e1](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/2d243e1fbf3983d8467174fad20dd0deaedfc78a))
* set new action to release ([deb2774](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/deb2774a11eea1ed504e23c9adb2403efcb214f1))
* set the main class ([8ea307c](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/8ea307c13e295b9e000167de36654b504141ea9e))

### General maintenance

* add bulk relationship deletion ([ee56f02](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/ee56f02d86515f3254ae4e97f1f9b0b667113e16))
* add Dockerfile ([07e6b53](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/07e6b538bc461dc17dd9df6e5ee125d6eafc8c04))
* add gitattributes ([c567d2d](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/c567d2d2ff8e183d7a4a321598ccd5f532f5e240))
* add license ([582cf7a](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/582cf7aa629788b795b3dfaf2fde9d6f19d93c26))
* align ontology to domain tags ([8338a64](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/8338a64ffcbf6f42a5a59fadbf7cbb58012fa452))
* align tm model with domain tag ([1d1531e](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/1d1531e29f7516cef3eda536a0a8ea23649c71b8))
* delete unnecessary files ([022b46e](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/022b46ea30a5679b8dd55c3f1f490acf7a0230e6))
* delete unused config file ([dce8e47](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/dce8e47674c70728b6fea6084b911c8734c6ef3b))
* delete unused test resources and source ([8576b65](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/8576b65d1db986cec862466d0bbc3e24c443061e))
* include ditto configurations and dt version in service config ([98c9e71](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/98c9e715d51b41b1475e2a0ae64d9ca760665cf1))
* merge pull request [#1](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/issues/1) from Web-of-Digital-Twins/build/gradle-migration ([c1712b9](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/c1712b98b83e7423ab60c3e27baae73bd704bbe1))
* merge pull request [#2](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/issues/2) from Web-of-Digital-Twins/feat/base-alignment-dtd-1.0.0 ([03cf1ce](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/03cf1ce1e3087c31c9ef8a4ef06fbd331f438e5f))
* remove unnecessary imports ([f84307c](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/f84307c00170189bbbab54988e6ad27804a85c2f))
* update DTD terminology ([7bf3c79](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/7bf3c7900adac4193bcb7736615d8d6246c21f78))
* update gitignore ([6ad8552](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/6ad855238d58697b645778ed53e60fcf560f220e))
* update gitignore ([a9fc22e](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/a9fc22e5495a4208e63680e09763a2124f7aa49d))

### Refactoring

* pass all the configurations via environmental variables, and centralize its management ([455482e](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/455482ef6af4fd34b3148025601e9ce9b717af46))
* rename rdf property ([583c758](https://github.com/Web-of-Digital-Twins/ditto-wodt-adapter/commit/583c75822bb2d117a645739805fb4988b1fa1862))

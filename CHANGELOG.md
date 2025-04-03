# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-01-30

### Added

- UI files added.
- Screenshots in the `README.md`.
- Loading screen while fetching the data.

### Removed

- The Model View Controller classes, due to using the `MWT` library now.
- `updateInformation()` method, not useful, the information only update once.
- The `SliderWithValue` class, was not working as intended and a better alternative has been found.
- Tests were not working, and they were not really useful. So they got removed.

### Changed

- `README.md` updated.
- Using `MWT` instead of only using `MicroUI`, it looks easier and more appropriate for my app.
- `getWeatherIcon()` is now returning the path of the image, more comprehensive and easier to test.
- Complete UI redesign, now using the `Carousel` for a better user experience.
- `LocationProvider` deactivated due to reaching the API limit. The app now only tells the weather in Nantes.

### Fixed

- Fixed lambdas expressions by modifying the `gradle.properties` file.

## [0.1.0] - 2025-01-28

### Added

- All current project files.
- `CHANGELOG.md` file.
- `package-info.java` file.
- Some comments on the `updateInformation` method.

### Changed

- `README.md` updated.
- Removed the UI classes, they do not belong in this feature.
- Stopped using the `JSONResource` class, it was not working at all, throwing an InternalLimitsError when used. Surely
  due to a bug in the `Resty` library. Using `JSONObject` instead.
- Changed the local VEE port to one in an artifact repository.

### Fixed

- Fixed the Jenkins build problems by changing the `LICENSE.txt`, replacing `buildWithMMM` with `buildWithGradle` and
  fixing other minor problems.
- Changed the VEE port from `M5QNX_eval:2.2.0` to `R0OUY_eval:2.3.0` to fix the connection problem on the board.
- Fixed the Sonar issues.

---

<small>
Copyright 2025 MicroEJ Corp. All rights reserved.
Use of this source code is governed by a BSD-style license that can be found with this software.
</small>
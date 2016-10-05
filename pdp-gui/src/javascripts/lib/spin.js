// Hihi sneaky global

const spinner = {
  onStart: null,
  onStop: null,

  start: () => spinner.onStart && spinner.onStart(),
  stop: () => spinner.onStop && spinner.onStop()
};

export default spinner;

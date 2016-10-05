export const determineStatus = decision => {
  switch (decision) {
  case "Permit":
    {
      return "check";
    }
  case "Indeterminate":
  case "Deny":
    {
      return "remove";
    }
  case "NotApplicable":
    {
      return "question";
    }
  default:
    {
      throw "Unknown decision" + decision;
    }
  }
};
